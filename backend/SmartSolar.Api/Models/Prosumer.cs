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
    // Uses the NIC as the unique Prosumer identifier.
    [BsonId]
    public string Nic { get; set; } =
        string.Empty;

    // Stores the Prosumer's full name.
    public string Name { get; set; } =
        string.Empty;

    // Stores the Prosumer's email address.
    public string Email { get; set; } =
        string.Empty;

    // Stores the Prosumer's contact number.
    public string Phone { get; set; } =
        string.Empty;

    // Stores the Prosumer's residential address.
    public string Address { get; set; } =
        string.Empty;

    // Tracks the account status, initially Pending.
    public string Status { get; set; } =
        AccountStatuses.Pending;

    // Records when the account was created.
    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;

    // Records when the account was last updated.
    public DateTime UpdatedAt { get; set; } =
        DateTime.UtcNow;

    // Records when account deactivation was requested.
    public DateTime? DeactivationRequestedAt
    {
        get;
        set;
    }

    // Records when the account was deactivated.
    public DateTime? DeactivatedAt
    {
        get;
        set;
    }

    // Records when the account was reactivated.
    public DateTime? ReactivatedAt
    {
        get;
        set;
    }
}