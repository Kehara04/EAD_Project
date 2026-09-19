using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartSolar.Api.Models;

public class SolarStation
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    public string Name { get; set; } =
        string.Empty;

    public string Address { get; set; } =
        string.Empty;

    public double Latitude { get; set; }

    public double Longitude { get; set; }

    public double CapacityKw { get; set; }

    public int TotalSlots { get; set; }

    public int AvailableSlots { get; set; }

    public string OpeningTime { get; set; } =
        string.Empty;

    public string ClosingTime { get; set; } =
        string.Empty;

    public string Status { get; set; } =
        "Active";

    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;

    public DateTime UpdatedAt { get; set; } =
        DateTime.UtcNow;
}