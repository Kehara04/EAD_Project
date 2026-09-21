using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

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