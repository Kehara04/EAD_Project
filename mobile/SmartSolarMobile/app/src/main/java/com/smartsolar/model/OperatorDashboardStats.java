package com.smartsolar.model;

import java.util.List;

/**
 * Mirrors OperatorDashboardResponse from the C# Web API.
 * Returned by GET /api/operator/dashboard.
 * Counts cover reservations scheduled for the current UTC date.
 */
public class OperatorDashboardStats {

    private int todayTotal;
    private int pendingCount;
    private int approvedCount;
    private int completedCount;
    private int cancelledCount;

    private List<StationSummary> stationSummaries;

    public int getTodayTotal()     { return todayTotal; }
    public int getPendingCount()   { return pendingCount; }
    public int getApprovedCount()  { return approvedCount; }
    public int getCompletedCount() { return completedCount; }
    public int getCancelledCount() { return cancelledCount; }

    public List<StationSummary> getStationSummaries() {
        return stationSummaries;
    }


    /**
     * Per-station completion summary nested inside OperatorDashboardStats.
     */
    public static class StationSummary {

        private String stationId;
        private String stationName;
        private int    completedToday;

        public String getStationId()     { return stationId; }
        public String getStationName()   { return stationName; }
        public int    getCompletedToday(){ return completedToday; }
    }
}
