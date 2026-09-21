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
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    public string Name { get; set; } =
        string.Empty;

    public string Email { get; set; } =
        string.Empty;

    public string PasswordHash { get; set; } =
        string.Empty;

    public string Role { get; set; } =
        string.Empty;

    public string Status { get; set; } =
        AccountStatuses.Active;

    /*
     * For a Prosumer this stores the NIC.
     * Backoffice and Grid Operator accounts normally
     * leave this field null.
     */
    public string? ReferenceId { get; set; }

    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;

    public DateTime UpdatedAt { get; set; } =
        DateTime.UtcNow;
}