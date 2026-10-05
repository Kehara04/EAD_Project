/*
 * File: User.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Represents an authenticated system user stored in MongoDB.
 */

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using SmartSolar.Api.Constants;

namespace SmartSolar.Api.Models;

public class User
{
    // Stores the unique MongoDB user identifier.
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    // Stores the user's full name.
    public string Name { get; set; } =
        string.Empty;

    // Stores the user's email address.
    public string Email { get; set; } =
        string.Empty;

    // Stores the securely hashed password.
    public string PasswordHash { get; set; } =
        string.Empty;

    // Identifies the user's role for access control.
    public string Role { get; set; } =
        string.Empty;

    // Tracks the user's account status.
    public string Status { get; set; } =
        AccountStatuses.Active;

    // Stores the Prosumer's NIC; null for other user roles.
    public string? ReferenceId { get; set; }

    // Records when the user account was created.
    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;
    
    // Records when the user account was last updated.
    public DateTime UpdatedAt { get; set; } =
        DateTime.UtcNow;
}