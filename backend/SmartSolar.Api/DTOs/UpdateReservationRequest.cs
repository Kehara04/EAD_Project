/*
 * File: UpdateReservationRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 * Description:
 * Defines the request data used to update an existing reservation.
 * Supports changes to the selected station, energy slot,
 * scheduled date and time, and reservation notes.
 */
using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

// Defines the updated reservation information submitted by a prosumer.
public class UpdateReservationRequest
{
    // Identifies the station selected for the updated reservation.
    [Required]
    public string StationId { get; set; } =
        string.Empty;

    // Identifies the selected energy booking slot.
    [Required]
    public string BookingSlotId { get; set; } =
        string.Empty;

    // Specifies the updated reservation date and time.
    [Required]
    public DateTime ScheduledAt { get; set; }

    // Stores optional reservation notes with a maximum of 300 characters.
    [StringLength(300)]
    public string? Notes { get; set; }
}