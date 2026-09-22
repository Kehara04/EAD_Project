package com.smartsolar.model;

public class UpdateReservationRequest {

    private final String stationId;
    private final String bookingSlotId;
    private final String scheduledAt;
    private final String notes;

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