/*
 * File: ReservationResponse.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 * Description:
 * Defines the reservation information returned by the API.
 * Combines reservation details with the associated prosumer,
 * station, booking slot, schedule, and current status.
 */
namespace SmartSolar.Api.DTOs;

// Provides reservation information to the web and mobile applications.
public class ReservationResponse
{
    // Stores the unique reservation ID.
    public string Id { get; set; } =
        string.Empty;

    // Identifies the Prosumer who created the reservation.
    public string ProsumerId { get; set; } =
        string.Empty;

    // Stores the Prosumer's name, if available.
    public string? ProsumerName { get; set; }

    // Identifies the selected microgrid station.
    public string StationId { get; set; } =
        string.Empty;

    // Stores the selected station's name.
    public string StationName { get; set; } =
        string.Empty;

    // Stores the selected station's address.
    public string StationAddress { get; set; } =
        string.Empty;

    // Identifies the selected energy booking slot.
    public string BookingSlotId { get; set; } =
        string.Empty;

    // Specifies the physical slot number at the station.s
    public int SlotNumber { get; set; }

    // Stores the scheduled date and time of the reservation.
    public DateTime ScheduledAt { get; set; }

    // Indicates the current reservation status.
    public string Status { get; set; } =
        string.Empty;

    // Stores optional notes associated with the reservation.
    public string? Notes { get; set; }

    // Records when the reservation was created.
    public DateTime CreatedAt { get; set; }

    // Records when the reservation was last updated.
    public DateTime UpdatedAt { get; set; }
}