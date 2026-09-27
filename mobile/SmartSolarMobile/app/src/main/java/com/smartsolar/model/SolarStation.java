package com.smartsolar.model;

/**
 * Represents a solar station in the Smart Solar Microgrid Trading System.
 * Stores station identification, location, energy capacity, booking slot
 * availability, operating hours, and current operational status.
 */
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

    // Returns the unique identifier of the solar station.
    public String getId() {
        return id;
    }

    // Returns the name of the solar station.
    public String getName() {
        return name;
    }

    // Returns the address of the solar station.
    public String getAddress() {
        return address;
    }

    // Returns the station's latitude in decimal degrees for map positioning.
    public double getLatitude() {
        return latitude;
    }

    // Returns the station's longitude in decimal degrees for map positioning.
    public double getLongitude() {
        return longitude;
    }

    // Returns the energy capacity of the solar station in kilowatts.
    public double getCapacityKw() {
        return capacityKw;
    }

    // Returns the total number of booking slots at the station.
    public int getTotalSlots() {
        return totalSlots;
    }

    // Returns the number of available booking slots at the station.
    public int getAvailableSlots() {
        return availableSlots;
    }

    // Returns the station's opening time as provided by the backend API.
    public String getOpeningTime() {
        return openingTime;
    }

    // Returns the station's closing time as provided by the backend API.
    public String getClosingTime() {
        return closingTime;
    }

    // Returns the current operational status of the solar station.
    public String getStatus() {
        return status;
    }
}