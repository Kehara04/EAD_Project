/*
 * File: CreateReservationRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 * Description:
 * Defines the request data required to create an energy reservation.
 * Accepts the selected station, booking slot, scheduled date
 * and time, and optional notes from the prosumer.
 * Reference:Tutorial - Create a Web API with ASP.NET Core Controllers.
 * https://www.youtube.com/watch?v=Y2DpFNHtjA8
 */
using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

// Defines the information received from the mobile application when creating a reservation.
public class CreateReservationRequest
{
    // Identifies the microgrid station selected for the reservation.
    [Required(
        ErrorMessage =
            "Station is required."
    )]
    public string StationId { get; set; } =
        string.Empty;

    // Identifies the energy booking slot selected by the Prosumer.
    [Required(
        ErrorMessage =
            "Booking slot is required."
    )]
    public string BookingSlotId { get; set; } =
        string.Empty;

    // Specifies the scheduled date and time of the reservation.
    [Required(
        ErrorMessage =
            "Reservation date and time are required."
    )]
    public DateTime ScheduledAt { get; set; }

    // Stores optional reservation notes with a maximum length of 300 characters.
    [StringLength(
        300,
        ErrorMessage =
            "Notes cannot exceed 300 characters."
    )]
    public string? Notes { get; set; }
}