/*
 * File: QrPayload.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Represents the data structure that is serialised to JSON
 * and encoded into a QR code image for an approved reservation.
 * The signature field contains an HMAC-SHA256 digest that the
 * server verifies before accepting any operator scan request.
 * Reference:Tutorial - Create a Web API with ASP.NET Core Controllers.
 * https://www.youtube.com/watch?v=Y2DpFNHtjA8
 */

namespace SmartSolar.Api.DTOs;

public class QrPayload
{
    // MongoDB ObjectId of the approved reservation.
    public string ReservationId { get; set; } =
        string.Empty;


    // NIC of the prosumer who owns the reservation.
    public string ProsumerId { get; set; } =
        string.Empty;


    // MongoDB ObjectId of the target station.
    public string StationId { get; set; } =
        string.Empty;


    // Physical slot number at the station.
    public int SlotNumber { get; set; }


    // UTC ISO-8601 string of the reserved time window.
    public string ScheduledAt { get; set; } =
        string.Empty;


    // UTC ISO-8601 string of when this QR was generated.
    public string IssuedAt { get; set; } =
        string.Empty;


    // Stores the HMAC-SHA256 signature used to verify QR code authenticity.
    public string Signature { get; set; } =
        string.Empty;
}
