package com.smartsolar.model;

/*
 Represents the request body used to create an energy slot reservation.
 Stores the selected station, booking slot, scheduled date and time,
 and optional reservation notes.
 */
public class CreateReservationRequest {

    private final String stationId;
    private final String bookingSlotId;
    private final String scheduledAt;
    private final String notes;

    // Initializes a reservation request with the selected station, slot, schedule, and notes.
    public CreateReservationRequest(
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