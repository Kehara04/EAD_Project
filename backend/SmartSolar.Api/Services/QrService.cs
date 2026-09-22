/*
 * File: QrService.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Provides HMAC-SHA256 signed QR payload generation and
 * verification logic for the energy-transfer flow.
 * All business rules for the QR lifecycle are enforced here.
 */

using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using MongoDB.Driver;
using SmartSolar.Api.Constants;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class QrService
{
    private readonly MongoDbContext _context;

    // Secret key loaded from the QR_SECRET environment variable.
    private readonly string _secret;


    public QrService(MongoDbContext context)
    {
        _context = context;

        /*
         * Read the signing secret at construction time.
         * A missing or empty key is rejected so the application
         * cannot start in an insecure state.
         */
        _secret =
            Environment.GetEnvironmentVariable(
                "QR_SECRET"
            ) ?? string.Empty;

        if (string.IsNullOrWhiteSpace(_secret))
        {
            throw new InvalidOperationException(
                "QR_SECRET is missing from the .env file. " +
                "Set a random 32-character hex value before starting the API."
            );
        }
    }


    /* =========================================
       GENERATE QR PAYLOAD
    ========================================= */

    /// <summary>
    /// Builds a signed QrPayload for the given approved reservation.
    /// Throws InvalidOperationException when the reservation does not
    /// exist or is not in the Approved state.
    /// </summary>
    public async Task<QrPayload> GenerateQrPayloadAsync(
        string reservationId,
        string requestingProsumerId)
    {
        // Load the reservation from the database.
        var reservation =
            await _context.Reservations
                .Find(x =>
                    x.Id == reservationId
                )
                .FirstOrDefaultAsync();


        if (reservation == null)
        {
            throw new InvalidOperationException(
                "Reservation was not found."
            );
        }


        // Only the owning Prosumer may generate a QR for a reservation.
        if (reservation.ProsumerId != requestingProsumerId)
        {
            throw new InvalidOperationException(
                "You are not authorised to generate a QR code for this reservation."
            );
        }


        // Only approved reservations may produce a QR code.
        if (!reservation.Status.Equals(
                ReservationStatuses.Approved,
                StringComparison.OrdinalIgnoreCase))
        {
            throw new InvalidOperationException(
                "A QR code can only be generated for an Approved reservation. " +
                $"Current status: {reservation.Status}."
            );
        }


        var issuedAt = DateTime.UtcNow;


        var payload = new QrPayload
        {
            ReservationId =
                reservation.Id!,

            ProsumerId =
                reservation.ProsumerId,

            StationId =
                reservation.StationId,

            SlotNumber =
                reservation.SlotNumber,

            ScheduledAt =
                reservation.ScheduledAt
                    .ToString("o"), // ISO 8601 round-trip

            IssuedAt =
                issuedAt.ToString("o"),

            Signature =
                ComputeSignature(
                    reservation.Id!,
                    reservation.ProsumerId,
                    issuedAt.ToString("o")
                )
        };


        return payload;
    }


    /* =========================================
       VERIFY QR PAYLOAD
    ========================================= */

    /// <summary>
    /// Verifies the scanned QR payload sent by a Grid Operator.
    /// Always returns a VerifyQrResponse; isValid indicates success.
    /// </summary>
    public async Task<VerifyQrResponse> VerifyQrAsync(
        VerifyQrRequest request)
    {
        // Step 1 – verify the HMAC signature.
        var expectedSignature =
            ComputeSignature(
                request.ReservationId,
                request.ProsumerId,
                request.IssuedAt
            );


        if (!expectedSignature.Equals(
                request.Signature,
                StringComparison.OrdinalIgnoreCase))
        {
            return InvalidResponse(
                request,
                "QR code signature is invalid. The code may have been tampered with."
            );
        }


        // Step 2 – load the reservation.
        var reservation =
            await _context.Reservations
                .Find(x =>
                    x.Id == request.ReservationId
                )
                .FirstOrDefaultAsync();


        if (reservation == null)
        {
            return InvalidResponse(
                request,
                "Reservation was not found on the server."
            );
        }


        // Step 3 – cross-check prosumer ownership.
        if (reservation.ProsumerId != request.ProsumerId)
        {
            return InvalidResponse(
                request,
                "QR code does not match the reservation owner."
            );
        }


        // Step 4 – cross-check station.
        if (reservation.StationId != request.StationId)
        {
            return InvalidResponse(
                request,
                "QR code station does not match the reservation."
            );
        }


        // Step 5 – check reservation status.
        if (reservation.Status.Equals(
                ReservationStatuses.Completed,
                StringComparison.OrdinalIgnoreCase))
        {
            return InvalidResponse(
                request,
                "This reservation has already been completed."
            );
        }

        if (reservation.Status.Equals(
                ReservationStatuses.Cancelled,
                StringComparison.OrdinalIgnoreCase))
        {
            return InvalidResponse(
                request,
                "This reservation has been cancelled."
            );
        }

        if (!reservation.Status.Equals(
                ReservationStatuses.Approved,
                StringComparison.OrdinalIgnoreCase))
        {
            return InvalidResponse(
                request,
                $"Reservation is not in an Approved state. Current status: {reservation.Status}."
            );
        }


        // Load prosumer and station display names for the operator UI.
        var prosumer =
            await _context.Prosumers
                .Find(x =>
                    x.Nic == reservation.ProsumerId
                )
                .FirstOrDefaultAsync();

        var station =
            await _context.Stations
                .Find(x =>
                    x.Id == reservation.StationId
                )
                .FirstOrDefaultAsync();


        return new VerifyQrResponse
        {
            IsValid = true,

            ReservationId =
                reservation.Id!,

            ProsumerId =
                reservation.ProsumerId,

            ProsumerName =
                prosumer?.Name,

            StationName =
                station?.Name,

            SlotNumber =
                reservation.SlotNumber,

            ScheduledAt =
                reservation.ScheduledAt
                    .ToString("o"),

            Status =
                reservation.Status,

            Message =
                "QR code verified. You may confirm the energy transfer."
        };
    }


    /* =========================================
       HELPERS
    ========================================= */

    /// <summary>
    /// Computes the HMAC-SHA256 hex digest used for payload signing.
    /// The message is: reservationId|prosumerId|issuedAt
    /// </summary>
    private string ComputeSignature(
        string reservationId,
        string prosumerId,
        string issuedAt)
    {
        var message =
            $"{reservationId}|{prosumerId}|{issuedAt}";

        var keyBytes =
            Encoding.UTF8.GetBytes(_secret);

        var msgBytes =
            Encoding.UTF8.GetBytes(message);

        using var hmac =
            new HMACSHA256(keyBytes);

        var hash = hmac.ComputeHash(msgBytes);

        return Convert.ToHexString(hash)
            .ToLowerInvariant();
    }


    /// <summary>
    /// Builds a failed VerifyQrResponse with minimal echo-back data.
    /// </summary>
    private static VerifyQrResponse InvalidResponse(
        VerifyQrRequest request,
        string message)
    {
        return new VerifyQrResponse
        {
            IsValid = false,

            ReservationId =
                request.ReservationId,

            ProsumerId =
                request.ProsumerId,

            ScheduledAt =
                request.ScheduledAt,

            SlotNumber =
                request.SlotNumber,

            Status = string.Empty,

            Message = message
        };
    }
}
