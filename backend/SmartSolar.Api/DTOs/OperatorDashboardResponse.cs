/*
 * File: OperatorDashboardResponse.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Aggregated statistics returned by GET /api/operator/dashboard.
 * Counts cover reservations scheduled for the current UTC date.
 */

namespace SmartSolar.Api.DTOs;

public class OperatorDashboardResponse
{
    // Total reservations scheduled for today (all statuses).
    public int TodayTotal { get; set; }


    // Number of today's reservations still in Pending state.
    public int PendingCount { get; set; }


    // Number of today's reservations in Approved state.
    public int ApprovedCount { get; set; }


    // Number of today's reservations marked Completed.
    public int CompletedCount { get; set; }


    // Number of today's reservations that were Cancelled.
    public int CancelledCount { get; set; }


    // Per-station breakdown of completed transfers today.
    public List<StationDashboardSummary> StationSummaries { get; set; } =
        new List<StationDashboardSummary>();
    // Recent completed reservations history
    public List<ReservationResponse> CompletedHistory { get; set; } =
        new List<ReservationResponse>();
}


/*
 * Per-station completion summary embedded inside
 * OperatorDashboardResponse.
 */
public class StationDashboardSummary
{
    // MongoDB ObjectId of the station.
    public string StationId { get; set; } =
        string.Empty;


    // Display name of the station.
    public string StationName { get; set; } =
        string.Empty;


    // Number of completed transfers at this station today.
    public int CompletedToday { get; set; }
}
