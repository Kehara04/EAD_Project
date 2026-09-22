/*
 * File: VerifyQrResponse.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Returned by POST /api/operator/verify after the server
 * has validated the scanned QR payload against the database.
 * Always returns HTTP 200; isValid carries the pass/fail result.
 */

namespace SmartSolar.Api.DTOs;

public class VerifyQrResponse
{
    // True when the QR is authentic and the reservation is Approved.
    public bool IsValid { get; set; }


    // MongoDB ObjectId of the reservation, echoed back for the client.
    public string ReservationId { get; set; } =
        string.Empty;


    // Display name of the prosumer, or null when lookup fails.
    public string? ProsumerName { get; set; }


    // NIC of the prosumer.
    public string ProsumerId { get; set; } =
        string.Empty;


    // Display name of the station.
    public string? StationName { get; set; }


    // Physical slot number.
    public int SlotNumber { get; set; }


    // UTC ISO-8601 scheduled time of the reservation.
    public string ScheduledAt { get; set; } =
        string.Empty;


    // Current status of the reservation (e.g. Approved, Completed).
    public string Status { get; set; } =
        string.Empty;


    // Human-readable description of the verification result.
    public string Message { get; set; } =
        string.Empty;
}
