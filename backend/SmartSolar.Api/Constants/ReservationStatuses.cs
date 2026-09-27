/*
 * File: ReservationStatuses.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 * Description:
 * Defines the supported reservation status values.
 * Provides consistent status names across reservation
 * creation, approval, cancellation, and completion.
 */
namespace SmartSolar.Api.Constants;

// Centralizes reservation status constants to avoid inconsistent status values.
public static class ReservationStatuses
{
    public const string Pending = "Pending";

    public const string Approved = "Approved";

    public const string Cancelled = "Cancelled";

    public const string Completed = "Completed";
}