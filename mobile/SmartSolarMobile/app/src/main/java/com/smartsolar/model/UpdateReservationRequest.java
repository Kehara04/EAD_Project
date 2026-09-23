/*
 * File: UpdateReservationRequest.java
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 *
 * Description:
 * Defines the request body used to modify an existing reservation.
 * Contains the updated station, booking slot, scheduled date and
 * time, and optional notes submitted to the backend API.
 */
package com.smartsolar.model;

public class UpdateReservationRequest {

    private final String stationId;
    private final String bookingSlotId;
    private final String scheduledAt;
    private final String notes;

    // Initializes the updated reservation details before sending them to the backend.
    public UpdateReservationRequest(
            String stationId,
            String bookingSlotId,
            String scheduledAt,
            String notes
    ) {
        this.stationId = stationId;
        this.bookingSlotId = bookingSlotId;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
    }
}