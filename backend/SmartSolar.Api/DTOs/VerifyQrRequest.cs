/*
 * File: VerifyQrRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Request body sent by the Grid Operator mobile client
 * after scanning a Prosumer QR code.  The payload mirrors
 * QrPayload and is re-validated by QrService on the server.
 * Reference:Tutorial - Create a Web API with ASP.NET Core Controllers.
 * https://www.youtube.com/watch?v=Y2DpFNHtjA8
 */

namespace SmartSolar.Api.DTOs;

public class VerifyQrRequest
{
    // MongoDB ObjectId of the reservation to verify.
    public string ReservationId { get; set; } =
        string.Empty;


    // NIC of the prosumer who owns the reservation.
    public string ProsumerId { get; set; } =
        string.Empty;


    // MongoDB ObjectId of the station.
    public string StationId { get; set; } =
        string.Empty;


    // Physical slot number encoded in the QR.
    public int SlotNumber { get; set; }


    // UTC ISO-8601 scheduled time encoded in the QR.
    public string ScheduledAt { get; set; } =
        string.Empty;


    // UTC ISO-8601 issuance time encoded in the QR.
    public string IssuedAt { get; set; } =
        string.Empty;


    // HMAC-SHA256 signature from the original QR payload.
    public string Signature { get; set; } =
        string.Empty;
}
