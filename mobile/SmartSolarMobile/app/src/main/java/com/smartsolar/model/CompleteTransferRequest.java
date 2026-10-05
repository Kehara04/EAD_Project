package com.smartsolar.model;

/*
 Body sent to POST /api/operator/complete.
 Mirrors CompleteTransferRequest from the C# Web API.
 */
public class CompleteTransferRequest {

    private String reservationId;
    private String prosumerId;

    // Initializes the request with the reservation and prosumer identifiers.
    public CompleteTransferRequest(
            String reservationId,
            String prosumerId) {

        this.reservationId = reservationId;
        this.prosumerId    = prosumerId;
    }
    // Returns the identifier of the reservation to be completed.
    public String getReservationId() { return reservationId; }

    // Returns the identifier of the prosumer associated with the transfer.
    public String getProsumerId()    { return prosumerId; }
}
