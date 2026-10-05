/*
 * File: EnergyReservation.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 * Description:
 * Defines the MongoDB model used to store energy reservations.
 * Maintains the relationship between a prosumer, solar station,
 * booking slot, scheduled date and reservation status.
 * Also stores timestamps used to track the reservation lifecycle.
 */
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartSolar.Api.Models;

// Represents a reservation created by a prosumer for an energy slot.
public class EnergyReservation
{
    // Stores the unique reservation ID.
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    // Identifies the prosumer who created the reservation.
    public string ProsumerId { get; set; } =
        string.Empty;

    // Identifies the selected solar station.
    [BsonRepresentation(BsonType.ObjectId)]
    public string StationId { get; set; } =
        string.Empty;


    // Identifies the reserved energy booking slot.
    [BsonRepresentation(BsonType.ObjectId)]
    public string BookingSlotId { get; set; } =
        string.Empty;

    // Stores the physical slot number.
    public int SlotNumber { get; set; }

    // Stores the scheduled reservation date and time.
    public DateTime ScheduledAt { get; set; }

    // Indicates the current reservation status.
    public string Status { get; set; } =
        "Pending";

    // Stores optional reservation notes.
    public string? Notes { get; set; }

    // Records when the reservation was created.
    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;

    // Records when the reservation was last updated.
    public DateTime UpdatedAt { get; set; } =
        DateTime.UtcNow;

    // Records when the reservation was approved.
    public DateTime? ApprovedAt { get; set; }

    // Records when the reservation was cancelled.
    public DateTime? CancelledAt { get; set; }

    // Records when the reservation was completed.
    public DateTime? CompletedAt { get; set; }
}