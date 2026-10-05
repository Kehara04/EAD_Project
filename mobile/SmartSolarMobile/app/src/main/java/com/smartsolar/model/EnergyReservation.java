package com.smartsolar.model;

/*
 Represents an energy reservation made by a prosumer.
 Stores reservation details, station and booking slot information,
 scheduled date and time, reservation status, notes, and timestamps.
 */
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

    // Returns the unique identifier of the reservation.
    public String getId() {
        return id;
    }

    // Returns the identifier of the prosumer who made the reservation.
    public String getProsumerId() {
        return prosumerId;
    }

    // Returns the name of the prosumer associated with the reservation.
    public String getProsumerName() {
        return prosumerName;
    }

    // Returns the identifier of the selected solar station.
    public String getStationId() {
        return stationId;
    }

    // Returns the name of the selected solar station.
    public String getStationName() {
        return stationName;
    }

    // Returns the address of the selected solar station.
    public String getStationAddress() {
        return stationAddress;
    }

    // Returns the identifier of the reserved energy booking slot.
    public String getBookingSlotId() {
        return bookingSlotId;
    }

    // Returns the number of the reserved booking slot.
    public int getSlotNumber() {
        return slotNumber;
    }

    // Returns the scheduled date and time of the reservation.
    public String getScheduledAt() {
        return scheduledAt;
    }

    // Returns the current status of the reservation.
    public String getStatus() {
        return status;
    }

    // Returns any additional notes associated with the reservation.
    public String getNotes() {
        return notes;
    }

    // Returns the date and time when the reservation was created.
    public String getCreatedAt() {
        return createdAt;
    }

    // Returns the date and time when the reservation was last updated.
    public String getUpdatedAt() {
        return updatedAt;
    }
}