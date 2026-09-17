/*
 * File: AccountStatuses.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Defines account and Prosumer lifecycle status values.
 */

namespace SmartSolar.Api.Constants;

public static class AccountStatuses
{
    public const string Pending = "Pending";

    public const string Active = "Active";

    public const string DeactivationRequested =
        "DeactivationRequested";

    public const string Deactivated = "Deactivated";
}