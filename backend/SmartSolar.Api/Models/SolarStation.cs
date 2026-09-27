/*
 * File: SolarStation.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Defines the MongoDB station document and the location, capacity,
 * availability and operating details returned to web and mobile clients.
 */

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartSolar.Api.Models;

public class SolarStation
{
    // Stores the unique MongoDB station identifier.
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

     // Stores the station name.
    public string Name { get; set; } =
        string.Empty;

    // Stores the station address.
    public string Address { get; set; } =
        string.Empty;

    // Decimal-degree coordinates are used directly by mobile map markers.
    public double Latitude { get; set; }

    public double Longitude { get; set; }

    // Station energy capacity is expressed in kilowatts.
    public double CapacityKw { get; set; }

    // TotalSlots is physical capacity; AvailableSlots is the stored free-slot count.
    public int TotalSlots { get; set; }

    public int AvailableSlots { get; set; }

    // Local operating-time strings; validation happens when a station is saved.
    public string OpeningTime { get; set; } =
        string.Empty;

    public string ClosingTime { get; set; } =
        string.Empty;

    // New stations are Active; status changes must pass the service business rules.
    public string Status { get; set; } =
        "Active";

    // Use UTC for audit timestamps, independent of the client timezone.
    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;

    public DateTime UpdatedAt { get; set; } =
        DateTime.UtcNow;
}