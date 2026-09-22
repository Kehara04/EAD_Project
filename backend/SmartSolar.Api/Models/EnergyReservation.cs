// using MongoDB.Bson;
// using MongoDB.Bson.Serialization.Attributes;

// namespace SmartSolar.Api.Models;

// public class EnergyReservation
// {
//     [BsonId]
//     [BsonRepresentation(BsonType.ObjectId)]
//     public string? Id { get; set; }


//     [BsonRepresentation(BsonType.ObjectId)]
//     public string ProsumerId { get; set; } =
//         string.Empty;


//     [BsonRepresentation(BsonType.ObjectId)]
//     public string StationId { get; set; } =
//         string.Empty;


//     [BsonRepresentation(BsonType.ObjectId)]
//     public string BookingSlotId { get; set; } =
//         string.Empty;


//     public int SlotNumber { get; set; }


//     public DateTime ScheduledAt { get; set; }


//     public string Status { get; set; } =
//         "Pending";


//     public string? Notes { get; set; }


//     public DateTime CreatedAt { get; set; } =
//         DateTime.UtcNow;


//     public DateTime UpdatedAt { get; set; } =
//         DateTime.UtcNow;


//     public DateTime? ApprovedAt { get; set; }


//     public DateTime? CancelledAt { get; set; }


//     public DateTime? CompletedAt { get; set; }
// }


using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartSolar.Api.Models;

public class EnergyReservation
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }


    // Prosumer uses NIC as its MongoDB primary key.
    // Therefore this must remain a normal string,
    // NOT a MongoDB ObjectId.
    public string ProsumerId { get; set; } =
        string.Empty;


    // Station IDs are MongoDB ObjectIds.
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