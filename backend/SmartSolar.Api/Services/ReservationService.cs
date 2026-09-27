/*
 * File: ReservationService.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 * Description:
 * Handles reservation creation, retrieval, updates, cancellation,
 * approval, slot availability, and booking business rules.
 */

using MongoDB.Driver;
using SmartSolar.Api.Constants;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class ReservationService
{
    private readonly MongoDbContext _context;


    // Initializes the MongoDB context.
    public ReservationService(
        MongoDbContext context)
    {
        _context = context;
    }

    // Retrieves all reservations for Backoffice management.
    public async Task<List<ReservationResponse>>
        GetAllAsync(
            string? status = null,
            string? search = null)
    {
        // Retrieves reservations ordered by scheduled date.
        var reservations =
            await _context.Reservations
                .Find(
                    Builders<EnergyReservation>
                        .Filter.Empty
                )
                .SortByDescending(
                    x => x.ScheduledAt
                )
                .ToListAsync();


        if (
            !string.IsNullOrWhiteSpace(status)
        )
        {
            reservations =
                reservations
                    .Where(x =>
                        x.Status.Equals(
                            status,
                            StringComparison
                                .OrdinalIgnoreCase
                        )
                    )
                    .ToList();
        }


        // Converts reservation records into API responses.
        var responses =
            new List<ReservationResponse>();


        foreach (
            var reservation
            in reservations
        )
        {
            var response =
                await ToResponseAsync(
                    reservation
                );

            responses.Add(response);
        }


        if (
            !string.IsNullOrWhiteSpace(search)
        )
        {
            var query =
                search.Trim();

            responses =
                responses
                    .Where(x =>
                        x.StationName
                            .Contains(
                                query,
                                StringComparison
                                    .OrdinalIgnoreCase
                            )
                        ||
                        (
                            x.ProsumerName != null &&
                            x.ProsumerName
                                .Contains(
                                    query,
                                    StringComparison
                                        .OrdinalIgnoreCase
                                )
                        )
                        ||
                        x.Status
                            .Contains(
                                query,
                                StringComparison
                                    .OrdinalIgnoreCase
                            )
                    )
                    .ToList();
        }


        return responses;
    }

    // Retrieves reservations belonging to the authenticated prosumer with optional filtering.
    public async Task<List<ReservationResponse>>
        GetForProsumerAsync(
            string prosumerId,
            string? status = null,
            string? search = null)
    {
        var filter =
            Builders<EnergyReservation>
                .Filter.Eq(
                    x => x.ProsumerId,
                    prosumerId
                );


        var reservations =
            await _context.Reservations
                .Find(filter)
                .SortByDescending(
                    x => x.ScheduledAt
                )
                .ToListAsync();


        if (
            !string.IsNullOrWhiteSpace(status)
        )
        {
            reservations =
                reservations
                    .Where(x =>
                        x.Status.Equals(
                            status,
                            StringComparison
                                .OrdinalIgnoreCase
                        )
                    )
                    .ToList();
        }


        var responses =
            new List<ReservationResponse>();


        foreach (
            var reservation
            in reservations
        )
        {
            responses.Add(
                await ToResponseAsync(
                    reservation
                )
            );
        }


        if (
            !string.IsNullOrWhiteSpace(search)
        )
        {
            responses =
                responses
                    .Where(x =>
                        x.StationName
                            .Contains(
                                search,
                                StringComparison
                                    .OrdinalIgnoreCase
                            )
                        ||
                        x.Status
                            .Contains(
                                search,
                                StringComparison
                                    .OrdinalIgnoreCase
                            )
                    )
                    .ToList();
        }


        return responses;
    }

    // Retrieves a reservation using its unique identifier.
    public async Task<ReservationResponse?>
        GetByIdAsync(
            string id)
    {
        var reservation =
            await _context.Reservations
                .Find(x =>
                    x.Id == id
                )
                .FirstOrDefaultAsync();


        if (reservation == null)
            return null;


        return await ToResponseAsync(
            reservation
        );
    }

    // Creates or synchronizes bookable slots based on the station's configured total slot count.
    public async Task EnsureStationSlotsAsync(
        string stationId)
    {
        var station =
            await _context.Stations
                .Find(x =>
                    x.Id == stationId
                )
                .FirstOrDefaultAsync();


        if (station == null)
        {
            throw new InvalidOperationException(
                "Station was not found."
            );
        }


        var existing =
            await _context.BookingSlots
                .Find(x =>
                    x.StationId ==
                    stationId
                )
                .ToListAsync();


        for (
            int number = 1;
            number <= station.TotalSlots;
            number++
        )
        {
            if (
                existing.Any(x =>
                    x.SlotNumber ==
                    number
                )
            )
            {
                continue;
            }


            var slot =
                new EnergyBookingSlot
                {
                    StationId =
                        stationId,

                    SlotNumber =
                        number,

                    Label =
                        $"Slot {number}",

                    IsActive =
                        true,

                    CreatedAt =
                        DateTime.UtcNow
                };


            await _context
                .BookingSlots
                .InsertOneAsync(
                    slot
                );
        }

        foreach (
            var slot in existing
        )
        {
            var shouldBeActive =
                slot.SlotNumber <=
                station.TotalSlots;


            if (
                slot.IsActive !=
                shouldBeActive
            )
            {
                var update =
                    Builders<EnergyBookingSlot>
                        .Update
                        .Set(
                            x => x.IsActive,
                            shouldBeActive
                        );


                await _context
                    .BookingSlots
                    .UpdateOneAsync(
                        x => x.Id ==
                             slot.Id,
                        update
                    );
            }
        }
    }

    // Checks slot availability for the selected station, date, and time.
    public async Task<List<AvailableSlotResponse>>
        GetAvailableSlotsAsync(
            string stationId,
            DateTime scheduledAt)
    {
        ValidateBookingDate(
            scheduledAt
        );


        var station =
            await _context.Stations
                .Find(x =>
                    x.Id == stationId
                )
                .FirstOrDefaultAsync();


        if (station == null)
        {
            throw new InvalidOperationException(
                "Station was not found."
            );
        }


        if (
            !station.Status.Equals(
                "Active",
                StringComparison
                    .OrdinalIgnoreCase
            )
        )
        {
            throw new InvalidOperationException(
                "This station is currently inactive."
            );
        }


        await EnsureStationSlotsAsync(
            stationId
        );


        var slots =
            await _context.BookingSlots
                .Find(x =>
                    x.StationId ==
                    stationId &&
                    x.IsActive
                )
                .SortBy(
                    x => x.SlotNumber
                )
                .ToListAsync();


        var start =
            scheduledAt;


        var end =
            scheduledAt.AddHours(1);


        var reservations =
            await _context.Reservations
                .Find(x =>
                    x.StationId ==
                    stationId &&
                    x.Status !=
                        ReservationStatuses
                            .Cancelled
                )
                .ToListAsync();


        return slots
            .Select(slot =>
            {
                var occupied =
                    scheduledAt.TimeOfDay ==
                        TimeSpan.Zero
                        ? reservations.Any(
                            reservation =>
                                reservation.BookingSlotId == slot.Id
                                && reservation.ScheduledAt.Date == scheduledAt.Date
                        )
                        : reservations.Any(
                            reservation =>
                                reservation.BookingSlotId == slot.Id
                                && reservation.ScheduledAt < end
                                && reservation.ScheduledAt.AddHours(1) > start
                        );


                return new
                    AvailableSlotResponse
                {
                    SlotId =
                        slot.Id!,

                    SlotNumber =
                        slot.SlotNumber,

                    Label =
                        slot.Label,

                    Available =
                        !occupied
                };
            })
            .ToList();
    }

    // Validates the booking rules and creates a new pending energy reservation.
    public async Task<ReservationResponse>
        CreateAsync(
            string prosumerId,
            CreateReservationRequest request)
    {
        ValidateBookingDate(
            request.ScheduledAt
        );


        var station =
            await _context.Stations
                .Find(x =>
                    x.Id ==
                    request.StationId
                )
                .FirstOrDefaultAsync();


        if (station == null)
        {
            throw new InvalidOperationException(
                "Station was not found."
            );
        }


        if (
            !station.Status.Equals(
                "Active",
                StringComparison
                    .OrdinalIgnoreCase
            )
        )
        {
            throw new InvalidOperationException(
                "Reservations cannot be created for an inactive station."
            );
        }


        await EnsureStationSlotsAsync(
            station.Id!
        );


        var slot =
            await _context.BookingSlots
                .Find(x =>
                    x.Id ==
                    request.BookingSlotId
                    &&
                    x.StationId ==
                    request.StationId
                    &&
                    x.IsActive
                )
                .FirstOrDefaultAsync();


        if (slot == null)
        {
            throw new InvalidOperationException(
                "The selected booking slot is invalid."
            );
        }


        await EnsureSlotAvailableAsync(
            request.StationId,
            request.BookingSlotId,
            request.ScheduledAt,
            null
        );


        var prosumerReservations =
            await _context.Reservations
                .Find(x =>
                    x.ProsumerId ==
                    prosumerId &&
                    x.Status !=
                        ReservationStatuses
                            .Cancelled
                )
                .ToListAsync();


        var userConflict =
            prosumerReservations.Any(
                x =>
                    x.ScheduledAt <
                    request.ScheduledAt
                        .AddHours(1)
                    &&
                    x.ScheduledAt
                        .AddHours(1) >
                    request.ScheduledAt
            );


        if (userConflict)
        {
            throw new InvalidOperationException(
                "You already have another reservation during this time."
            );
        }


        var reservation =
            new EnergyReservation
            {
                ProsumerId =
                    prosumerId,

                StationId =
                    request.StationId,

                BookingSlotId =
                    request.BookingSlotId,

                SlotNumber =
                    slot.SlotNumber,

                ScheduledAt =
                    request
                        .ScheduledAt,

                Status =
                    ReservationStatuses
                        .Pending,

                Notes =
                    request.Notes?
                        .Trim(),

                CreatedAt =
                    DateTime.UtcNow,

                UpdatedAt =
                    DateTime.UtcNow
            };


        await _context.Reservations
            .InsertOneAsync(
                reservation
            );


        return await ToResponseAsync(
            reservation
        );
    }
    
    // Updates an existing reservation after validating the twelve-hour rule and slot availability.
    public async Task<ReservationResponse?>
        UpdateAsync(
            string id,
            string prosumerId,
            UpdateReservationRequest request)
    {
        var reservation =
            await _context.Reservations
                .Find(x =>
                    x.Id == id &&
                    x.ProsumerId ==
                    prosumerId
                )
                .FirstOrDefaultAsync();


        if (reservation == null)
            return null;


        EnsureCanModify(
            reservation
        );


        ValidateBookingDate(
            request.ScheduledAt
        );


        var station =
            await _context.Stations
                .Find(x =>
                    x.Id ==
                    request.StationId
                )
                .FirstOrDefaultAsync();


        if (station == null)
        {
            throw new InvalidOperationException(
                "Station was not found."
            );
        }


        if (
            station.Status != "Active"
        )
        {
            throw new InvalidOperationException(
                "The selected station is inactive."
            );
        }


        await EnsureStationSlotsAsync(
            station.Id!
        );


        var slot =
            await _context.BookingSlots
                .Find(x =>
                    x.Id ==
                    request.BookingSlotId
                    &&
                    x.StationId ==
                    request.StationId
                    &&
                    x.IsActive
                )
                .FirstOrDefaultAsync();


        if (slot == null)
        {
            throw new InvalidOperationException(
                "The selected slot is invalid."
            );
        }


        await EnsureSlotAvailableAsync(
            request.StationId,
            request.BookingSlotId,
            request.ScheduledAt,
            reservation.Id
        );


        var update =
            Builders<EnergyReservation>
                .Update
                .Set(
                    x => x.StationId,
                    request.StationId
                )
                .Set(
                    x => x.BookingSlotId,
                    request.BookingSlotId
                )
                .Set(
                    x => x.SlotNumber,
                    slot.SlotNumber
                )
                .Set(
                    x => x.ScheduledAt,
                    request.ScheduledAt
                )
                .Set(
                    x => x.Notes,
                    request.Notes
                )

                .Set(
                    x => x.Status,
                    ReservationStatuses
                        .Pending
                )

                .Set(
                    x => x.ApprovedAt,
                    null
                )
                .Set(
                    x => x.UpdatedAt,
                    DateTime.UtcNow
                );


        await _context.Reservations
            .UpdateOneAsync(
                x => x.Id == id,
                update
            );


        return await GetByIdAsync(
            id
        );
    }

    // Cancels a reservation when at least twelve hours remain before its scheduled time.
    public async Task<ReservationResponse?>
        CancelAsync(
            string id,
            string prosumerId)
    {
        var reservation =
            await _context.Reservations
                .Find(x =>
                    x.Id == id &&
                    x.ProsumerId ==
                    prosumerId
                )
                .FirstOrDefaultAsync();


        if (reservation == null)
            return null;


        EnsureCanModify(
            reservation
        );


        var update =
            Builders<EnergyReservation>
                .Update
                .Set(
                    x => x.Status,
                    ReservationStatuses
                        .Cancelled
                )
                .Set(
                    x => x.CancelledAt,
                    DateTime.UtcNow
                )
                .Set(
                    x => x.UpdatedAt,
                    DateTime.UtcNow
                );


        await _context.Reservations
            .UpdateOneAsync(
                x => x.Id == id,
                update
            );


        return await GetByIdAsync(
            id
        );
    }

    // Approves a pending reservation following Backoffice authorization.
    public async Task<ReservationResponse?>
        ApproveAsync(
            string id)
    {
        var reservation =
            await _context.Reservations
                .Find(x =>
                    x.Id == id
                )
                .FirstOrDefaultAsync();


        if (reservation == null)
            return null;


        if (
            reservation.Status !=
            ReservationStatuses.Pending
        )
        {
            throw new InvalidOperationException(
                "Only pending reservations can be approved."
            );
        }


        if (
            reservation.ScheduledAt <=
            DateTime.UtcNow
        )
        {
            throw new InvalidOperationException(
                "Past reservations cannot be approved."
            );
        }


        var update =
            Builders<EnergyReservation>
                .Update
                .Set(
                    x => x.Status,
                    ReservationStatuses
                        .Approved
                )
                .Set(
                    x => x.ApprovedAt,
                    DateTime.UtcNow
                )
                .Set(
                    x => x.UpdatedAt,
                    DateTime.UtcNow
                );


        await _context.Reservations
            .UpdateOneAsync(
                x => x.Id == id,
                update
            );


        return await GetByIdAsync(
            id
        );
    }

    // Checks whether a station has active future reservations before allowing deactivation.
    public async Task<bool>
        HasActiveReservationsAsync(
            string stationId)
    {
        var statuses =
            new[]
            {
                ReservationStatuses.Pending,
                ReservationStatuses.Approved
            };


        return await _context.Reservations
            .Find(x =>
                x.StationId ==
                    stationId
                &&
                statuses.Contains(
                    x.Status
                )
                &&
                x.ScheduledAt >
                    DateTime.UtcNow
            )
            .AnyAsync();
    }

    // Validates that the reservation date is within the allowed booking period.
    private static void
        ValidateBookingDate(
            DateTime scheduledAt)
    {
        var now =
            DateTime.UtcNow;


        if (
            scheduledAt <= now
        )
        {
            throw new InvalidOperationException(
                "Reservation must be scheduled for a future time."
            );
        }


        if (
            scheduledAt >
            now.AddDays(7)
        )
        {
            throw new InvalidOperationException(
                "Reservations can only be scheduled within the next 7 days."
            );
        }
    }

    // Checks whether an existing reservation is eligible for modification.(12 hrs rule)
    private static void
        EnsureCanModify(
            EnergyReservation reservation)
    {
        if (
            reservation.Status ==
                ReservationStatuses
                    .Cancelled
            ||
            reservation.Status ==
                ReservationStatuses
                    .Completed
        )
        {
            throw new InvalidOperationException(
                "This reservation can no longer be modified."
            );
        }


        var notice =
            reservation.ScheduledAt -
            DateTime.UtcNow;


        if (
            notice <
            TimeSpan.FromHours(12)
        )
        {
            throw new InvalidOperationException(
                "Reservations can only be updated or cancelled with at least 12 hours' notice."
            );
        }
    }

    // Verifies that the selected station and booking slot are available.(prevent double booking)
    private async Task
        EnsureSlotAvailableAsync(
            string stationId,
            string slotId,
            DateTime scheduledAt,
            string? excludeReservationId)
    {
        var reservations =
            await _context.Reservations
                .Find(x =>
                    x.StationId ==
                    stationId
                    &&
                    x.BookingSlotId ==
                    slotId
                    &&
                    x.Status !=
                        ReservationStatuses
                            .Cancelled
                )
                .ToListAsync();


        var start =
            scheduledAt;

        var end =
            scheduledAt.AddHours(1);


        var conflict =
            scheduledAt.TimeOfDay ==
                TimeSpan.Zero
                ? reservations.Any(
                    x =>
                        x.Id != excludeReservationId
                        && x.ScheduledAt.Date == scheduledAt.Date
                )
                : reservations.Any(
                    x =>
                        x.Id != excludeReservationId
                        && x.ScheduledAt < end
                        && x.ScheduledAt.AddHours(1) > start
                );


        if (conflict)
        {
            throw new InvalidOperationException(
                "The selected slot is already reserved for this date or time."
            );
        }
    }

    // Converts reservation data into a response containing associated prosumer and station details.
    private async Task<ReservationResponse>
        ToResponseAsync(
            EnergyReservation reservation)
    {
        var station =
            await _context.Stations
                .Find(x =>
                    x.Id ==
                    reservation
                        .StationId
                )
                .FirstOrDefaultAsync();


        var prosumer =
            await _context.Prosumers
                .Find(x =>
                    x.Nic ==
                    reservation
                        .ProsumerId
                )
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
                station?.Name ??
                "Unknown Station",

            StationAddress =
                station?.Address ??
                "",

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