package com.smartsolar.model;

public class SolarStation {

    private String id;

    private String name;

    private String address;

    private double latitude;

    private double longitude;

    private double capacityKw;

    private int totalSlots;

    private int availableSlots;

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