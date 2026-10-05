/*
 * File: OperatorService.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Contains business logic for Grid Operator operations:
 * completing an energy transfer and retrieving the
 * operator dashboard statistics.
 * Reference:
 * https://www.youtube.com/watch?v=Y2DpFNHtjA8
 */

using MongoDB.Driver;
using SmartSolar.Api.Constants;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class OperatorService
{
    private readonly MongoDbContext _context;


    public OperatorService(MongoDbContext context)
    {
        _context = context;
    }


   
    //COMPLETE ENERGY TRANSFER
    public async Task<ReservationResponse> CompleteTransferAsync(
        CompleteTransferRequest request)
    {
        // Load the reservation by ID.
        var reservation =
            await _context.Reservations
                .Find(x =>
                    x.Id == request.ReservationId
                )
                .FirstOrDefaultAsync();


        if (reservation == null)
        {
            throw new InvalidOperationException(
                "Reservation was not found."
            );
        }


        // Verify the prosumer ID matches to prevent cross-account completion.
        if (reservation.ProsumerId != request.ProsumerId)
        {
            throw new InvalidOperationException(
                "The prosumer ID does not match the reservation owner."
            );
        }


        // Only Approved reservations can be completed.
        if (!reservation.Status.Equals(
                ReservationStatuses.Approved,
                StringComparison.OrdinalIgnoreCase))
        {
            throw new InvalidOperationException(
                $"Reservation cannot be completed. " +
                $"Current status: {reservation.Status}."
            );
        }


        var now = DateTime.UtcNow;


        // Persist Completed status and timestamps.
        var update =
            Builders<EnergyReservation>
                .Update
                .Set(x => x.Status,
                    ReservationStatuses.Completed)
                .Set(x => x.CompletedAt, now)
                .Set(x => x.UpdatedAt, now);


        await _context.Reservations
            .UpdateOneAsync(
                x => x.Id == request.ReservationId,
                update
            );


        return await BuildResponseAsync(
            request.ReservationId
        );
    }


    //OPERATOR DASHBOARD STATISTICS
    public async Task<OperatorDashboardResponse> GetDashboardAsync()
    {
        var todayStart = DateTime.UtcNow.Date;
        var todayEnd   = todayStart.AddDays(1);


        //Reservations scheduled for today 
        var scheduledToday =
            await _context.Reservations
                .Find(x =>
                    x.ScheduledAt >= todayStart &&
                    x.ScheduledAt <  todayEnd
                )
                .ToListAsync();


        //Reservations completed today (any scheduled date)
        var completedToday =
            await _context.Reservations
                .Find(x =>
                    x.Status == ReservationStatuses.Completed &&
                    x.CompletedAt.HasValue            &&
                    x.CompletedAt.Value >= todayStart &&
                    x.CompletedAt.Value <  todayEnd
                )
                .ToListAsync();


        //Build response
        var response = new OperatorDashboardResponse
        {
            TodayTotal = scheduledToday.Count,

            PendingCount =
                scheduledToday.Count(x =>
                    x.Status.Equals(
                        ReservationStatuses.Pending,
                        StringComparison.OrdinalIgnoreCase)
                ),

            ApprovedCount =
                scheduledToday.Count(x =>
                    x.Status.Equals(
                        ReservationStatuses.Approved,
                        StringComparison.OrdinalIgnoreCase)
                ),

            // Completed by action taken today (not just scheduled today).
            CompletedCount = completedToday.Count,

            CancelledCount =
                scheduledToday.Count(x =>
                    x.Status.Equals(
                        ReservationStatuses.Cancelled,
                        StringComparison.OrdinalIgnoreCase)
                )
        };


        //Per-station summaries (from completedToday)
        var byStation = completedToday
            .GroupBy(x => x.StationId)
            .ToList();

        foreach (var group in byStation)
        {
            var station =
                await _context.Stations
                    .Find(x => x.Id == group.Key)
                    .FirstOrDefaultAsync();

            response.StationSummaries.Add(
                new StationDashboardSummary
                {
                    StationId      = group.Key,
                    StationName    = station?.Name ?? "Unknown Station",
                    CompletedToday = group.Count()
                }
            );
        }

        //Recent Completed History
        var recentCompleted =
            await _context.Reservations
                .Find(x => x.Status == ReservationStatuses.Completed)
                .SortByDescending(x => x.CompletedAt)
                .Limit(10)
                .ToListAsync();

        foreach (var reservation in recentCompleted)
        {
            var resResponse = await BuildResponseAsync(reservation.Id!);
            response.CompletedHistory.Add(resResponse);
        }

        return response;
    }



    //HELPER – BUILD RESERVATION RESPONSE
    private async Task<ReservationResponse> BuildResponseAsync(
        string reservationId)
    {
        var reservation =
            await _context.Reservations
                .Find(x => x.Id == reservationId)
                .FirstOrDefaultAsync();


        if (reservation == null)
        {
            throw new InvalidOperationException(
                "Could not reload the reservation after update."
            );
        }


        // Fetch prosumer name.
        var prosumer =
            await _context.Prosumers
                .Find(x => x.Nic == reservation.ProsumerId)
                .FirstOrDefaultAsync();


        // Fetch station name and address.
        var station =
            await _context.Stations
                .Find(x => x.Id == reservation.StationId)
                .FirstOrDefaultAsync();


        return new ReservationResponse
        {
            Id =
                reservation.Id!,

            ProsumerId =
                reservation.ProsumerId,

            ProsumerName =
                prosumer?.Name,

            StationId =
                reservation.StationId,

            StationName =
                station?.Name ?? string.Empty,

            StationAddress =
                station?.Address ?? string.Empty,

            BookingSlotId =
                reservation.BookingSlotId,

            SlotNumber =
                reservation.SlotNumber,

            ScheduledAt =
                reservation.ScheduledAt,

            Status =
                reservation.Status,

            Notes =
                reservation.Notes,

            CreatedAt =
                reservation.CreatedAt,

            UpdatedAt =
                reservation.UpdatedAt
        };
    }
}
