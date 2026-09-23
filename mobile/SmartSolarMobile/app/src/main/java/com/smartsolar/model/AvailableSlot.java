/*
 * File: AvailableSlot.java
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 *
 * Description:
 * Represents an energy booking slot returned by the backend API.
 * Stores slot identification, slot number, label, and availability
 * information used during reservation creation and modification.
 */
package com.smartsolar.model;

public class AvailableSlot {

    private String slotId;
    private int slotNumber;
    private String label;
    private boolean available;

    // Returns the unique identifier of the energy booking slot.
    public String getSlotId() {
        return slotId;
    }

    // Returns the slot number assigned to the station.
    public int getSlotNumber() {
        return slotNumber;
    }

    // Returns the descriptive label of the booking slot.
    public String getLabel() {
        return label;
    }

    // Indicates whether the slot is available for the selected schedule.
    public boolean isAvailable() {
        return available;
    }
}