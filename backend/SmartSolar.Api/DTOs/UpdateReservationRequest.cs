/*
 * File: UpdateReservationRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 *
 * Description:
 * Defines the request data used to update an existing reservation.
 *
 * Supports changes to the selected station, energy slot,
 * scheduled date and time, and reservation notes.
 */
using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

// Defines the updated reservation information submitted by a prosumer.
public class UpdateReservationRequest
{
    [Required]
    public string StationId { get; set; } =
        string.Empty;


    [Required]
    public string BookingSlotId { get; set; } =
        string.Empty;


    [Required]
    public DateTime ScheduledAt { get; set; }


    [StringLength(300)]
    public string? Notes { get; set; }
}