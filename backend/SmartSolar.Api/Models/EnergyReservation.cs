/*
 * File: EnergyReservation.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 *
 * Description:
 * Defines the MongoDB model used to store energy reservations.
 *
 * Maintains the relationship between a prosumer, solar station,
 * booking slot, scheduled date and reservation status.
 *
 * Also stores timestamps used to track the reservation lifecycle.
 */
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartSolar.Api.Models;

// Represents a reservation created by a prosumer for an energy slot.
public class EnergyReservation
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    public string ProsumerId { get; set; } =
        string.Empty;

    [BsonRepresentation(BsonType.ObjectId)]
    public string StationId { get; set; } =
        string.Empty;


    // Booking slot IDs are MongoDB ObjectIds.
    [BsonRepresentation(BsonType.ObjectId)]
    public string BookingSlotId { get; set; } =
        string.Empty;


    public int SlotNumber { get; set; }


    public DateTime ScheduledAt { get; set; }


    public string Status { get; set; } =
        "Pending";


    public string? Notes { get; set; }


    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;


    public DateTime UpdatedAt { get; set; } =
        DateTime.UtcNow;


    public DateTime? ApprovedAt { get; set; }


    public DateTime? CancelledAt { get; set; }


    public DateTime? CompletedAt { get; set; }
}