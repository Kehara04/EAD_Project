/*
 * File: PasswordResetToken.cs
 * Project: Smart Solar Microgrid Trading System
 * Description: Stores password reset tokens and their expiration details.
 */

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartSolar.Api.Models;

public class PasswordResetToken
{
    // Stores the unique MongoDB identifier.
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    // Identifies the user requesting a password reset.
    public string UserId { get; set; } = string.Empty;

    // Store the token hash, never the original token.
    public string TokenHash { get; set; } = string.Empty;

    // Specifies when the reset token expires.
    public DateTime ExpiresAt { get; set; }

    // Records when the reset token was created.
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}