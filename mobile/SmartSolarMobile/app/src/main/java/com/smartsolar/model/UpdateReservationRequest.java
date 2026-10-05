package com.smartsolar.model;

/*
 Represents the request body used to update an existing energy reservation.
 Stores the selected station, booking slot, scheduled date and time,
 and optional reservation notes.
 */
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