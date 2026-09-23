/*
 * File: EnergyBookingSlot.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 *
 * Description:
 * Represents an individual bookable energy slot belonging
 * to a solar station.
 *
 * Each slot contains its station reference, slot number,
 * label, activation status, and creation timestamp.
 */
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartSolar.Api.Models;

// Defines the MongoDB model for an individual station booking slot.
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