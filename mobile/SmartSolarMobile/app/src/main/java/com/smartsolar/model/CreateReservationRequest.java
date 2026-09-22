package com.smartsolar.model;

public class CreateReservationRequest {

    private final String stationId;
    private final String bookingSlotId;
    private final String scheduledAt;
    private final String notes;

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