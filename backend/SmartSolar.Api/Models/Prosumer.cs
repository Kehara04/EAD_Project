/*
 * File: Prosumer.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Represents a solar Prosumer account.
 * NIC is used as the MongoDB primary key.
 */

using MongoDB.Bson.Serialization.Attributes;
using SmartSolar.Api.Constants;

namespace SmartSolar.Api.Models;

public class Prosumer
{
    [BsonId]
    public string Nic { get; set; } =
        string.Empty;

    public string Name { get; set; } =
        string.Empty;

    public string Email { get; set; } =
        string.Empty;

    public string Phone { get; set; } =
        string.Empty;

    public string Address { get; set; } =
        string.Empty;

    public string Status { get; set; } =
        AccountStatuses.Pending;

    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;

    public DateTime UpdatedAt { get; set; } =
        DateTime.UtcNow;

    public DateTime? DeactivationRequestedAt
    {
        get;
        set;
    }

    public DateTime? DeactivatedAt
    {
        get;
        set;
    }

    public DateTime? ReactivatedAt
    {
        get;
        set;
    }
}