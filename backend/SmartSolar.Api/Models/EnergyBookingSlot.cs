/*
 * File: EnergyBookingSlot.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 * Description:
 * Represents an individual bookable energy slot belonging
 * to a solar station.
 * Each slot contains its station reference, slot number,
 * label, activation status, and creation timestamp.
 */
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartSolar.Api.Models;

// Defines the MongoDB model for an individual station booking slot.
public class EnergyBookingSlot
{
    // Stores the unique MongoDB identifier of the booking slot.
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    // References the station to which this slot belongs.
    [BsonRepresentation(BsonType.ObjectId)]
    public string StationId { get; set; } =
        string.Empty;

    // Identifies the physical slot number within the station.
    public int SlotNumber { get; set; }

    // Stores the booking slot's display label.
    public string Label { get; set; } =
        string.Empty;

    // Indicates whether the slot is active.
    public bool IsActive { get; set; } =
        true;

    // Records when the booking slot was created.
    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;
}