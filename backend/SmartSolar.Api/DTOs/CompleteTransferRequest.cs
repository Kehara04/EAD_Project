/*
 * File: CompleteTransferRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Request body sent by the Grid Operator to finalise an
 * energy transfer after a successful QR verification.
 * The server re-validates ownership before marking
 * the reservation as Completed.
 */

namespace SmartSolar.Api.DTOs;

public class CompleteTransferRequest
{
    // MongoDB ObjectId of the reservation to complete.
    public string ReservationId { get; set; } =
        string.Empty;


    // NIC of the prosumer; cross-checked against the reservation.
    public string ProsumerId { get; set; } =
        string.Empty;
}
