namespace SmartSolar.Api.DTOs;

public class ReservationResponse
{
    public string Id { get; set; } =
        string.Empty;

    public string ProsumerId { get; set; } =
        string.Empty;

    public string? ProsumerName { get; set; }

    public string StationId { get; set; } =
        string.Empty;

    public string StationName { get; set; } =
        string.Empty;

    public string StationAddress { get; set; } =
        string.Empty;

    public string BookingSlotId { get; set; } =
        string.Empty;

    public int SlotNumber { get; set; }

    public DateTime ScheduledAt { get; set; }

    public string Status { get; set; } =
        string.Empty;

    public string? Notes { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}