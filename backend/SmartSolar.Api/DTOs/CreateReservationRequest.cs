using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

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