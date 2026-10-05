package com.smartsolar.model;

import java.util.List;

/*
 Mirrors OperatorDashboardResponse from the C# Web API.
 Returned by GET /api/operator/dashboard.
 Counts cover reservations scheduled for the current UTC date.
 */
public class OperatorDashboardStats {

    private int todayTotal;
    private int pendingCount;
    private int approvedCount;
    private int completedCount;
    private int cancelledCount;

    private List<StationSummary> stationSummaries;

    // Returns the total number of reservations scheduled for today.
    public int getTodayTotal()     { return todayTotal; }

    // Returns the number of pending reservations scheduled for today.
    public int getPendingCount()   { return pendingCount; }
    
    // Returns the number of approved reservations scheduled for today.
    public int getApprovedCount()  { return approvedCount; }

    // Returns the number of completed reservations scheduled for today.
    public int getCompletedCount() { return completedCount; }

    // Returns the number of cancelled reservations scheduled for today
    public int getCancelledCount() { return cancelledCount; }

    // Returns the reservation completion summaries for individual stations.
    public List<StationSummary> getStationSummaries() {
        return stationSummaries;
    }


    
    //Per-station completion summary nested inside OperatorDashboardStats.
    public static class StationSummary {

        private String stationId;
        private String stationName;
        private int    completedToday;

        // Returns the unique identifier of the solar station.
        public String getStationId()     { return stationId; }

        // Returns the name of the solar station.
        public String getStationName()   { return stationName; }

        // Returns the number of reservations completed at this station today.
        public int    getCompletedToday(){ return completedToday; }
    }
}
