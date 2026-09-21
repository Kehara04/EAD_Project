using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartSolar.Api.Models;

public class EnergyBookingSlot
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonRepresentation(BsonType.ObjectId)]
    public string StationId { get; set; } =
        string.Empty;

    public int SlotNumber { get; set; }

    public string Label { get; set; } =
        string.Empty;

    public bool IsActive { get; set; } =
        true;

    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;
}