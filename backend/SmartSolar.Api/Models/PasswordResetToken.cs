using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartSolar.Api.Models;

public class PasswordResetToken
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    public string UserId { get; set; } = string.Empty;

    // Store the token hash, never the original token.
    public string TokenHash { get; set; } = string.Empty;

    public DateTime ExpiresAt { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}