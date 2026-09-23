package com.smartsolar.model;

public class SolarStation {

    private String id;

    private String name;

    private String address;

    // Decimal degrees from the backend are used directly for OpenStreetMap marker positions.
    private double latitude;

    private double longitude;

    private double capacityKw;

    private int totalSlots;

    // Free-slot count supports availability filtering and the station detail display.
    private int availableSlots;

    // Operating hours are returned as display strings by the station API.
    private String openingTime;

    private String closingTime;

    private String status;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getCapacityKw() {
        return capacityKw;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public String getStatus() {
        return status;
    }
}