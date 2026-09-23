/*
 * File: CreateReservationRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 *
 * Description:
 * Defines the request data required to create an energy reservation.
 *
 * Accepts the selected station, booking slot, scheduled date
 * and time, and optional notes from the prosumer.
 */
using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

// Defines the information received from the mobile application when creating a reservation.
public class CreateReservationRequest
{
    [Required(
        ErrorMessage =
            "Station is required."
    )]
    public string StationId { get; set; } =
        string.Empty;


    [Required(
        ErrorMessage =
            "Booking slot is required."
    )]
    public string BookingSlotId { get; set; } =
        string.Empty;


    [Required(
        ErrorMessage =
            "Reservation date and time are required."
    )]
    public DateTime ScheduledAt { get; set; }


    [StringLength(
        300,
        ErrorMessage =
            "Notes cannot exceed 300 characters."
    )]
    public string? Notes { get; set; }
}