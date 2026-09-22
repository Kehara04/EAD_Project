package com.smartsolar.model;

/**
 * Body sent to POST /api/operator/complete.
 * Mirrors CompleteTransferRequest from the C# Web API.
 */
public class CompleteTransferRequest {

    private String reservationId;
    private String prosumerId;

    public CompleteTransferRequest(
            String reservationId,
            String prosumerId) {

        this.reservationId = reservationId;
        this.prosumerId    = prosumerId;
    }

    public String getReservationId() { return reservationId; }
    public String getProsumerId()    { return prosumerId; }
}
