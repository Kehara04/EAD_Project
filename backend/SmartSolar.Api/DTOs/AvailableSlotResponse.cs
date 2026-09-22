namespace SmartSolar.Api.DTOs;

public class AvailableSlotResponse
{
    public string SlotId { get; set; } =
        string.Empty;

    public int SlotNumber { get; set; }

    public string Label { get; set; } =
        string.Empty;

    public bool Available { get; set; }
}