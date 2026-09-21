package com.smartsolar.model;

public class EnergyReservation {

    private String id;

    private String prosumerId;
    private String prosumerName;

    private String stationId;
    private String stationName;
    private String stationAddress;

    private String bookingSlotId;
    private int slotNumber;

    private String scheduledAt;

    private String status;

    private String notes;

    private String createdAt;
    private String updatedAt;

    public String getId() {
        return id;
    }

    public String getProsumerId() {
        return prosumerId;
    }

    public String getProsumerName() {
        return prosumerName;
    }

    public String getStationId() {
        return stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public String getStationAddress() {
        return stationAddress;
    }

    public String getBookingSlotId() {
        return bookingSlotId;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public String getScheduledAt() {
        return scheduledAt;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}