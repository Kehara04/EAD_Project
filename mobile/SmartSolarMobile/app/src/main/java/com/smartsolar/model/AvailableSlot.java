package com.smartsolar.model;

public class AvailableSlot {

    private String slotId;
    private int slotNumber;
    private String label;
    private boolean available;

    public String getSlotId() {
        return slotId;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAvailable() {
        return available;
    }
}