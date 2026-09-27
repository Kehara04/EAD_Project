package com.smartsolar.model;

/*
 Mirrors VerifyQrResponse from the C# Web API.
 Populated from the POST /api/operator/verify response.
 isValid = true means the QR is authentic and the reservation is Approved.
 */
public class VerifyQrResponse {

    private boolean isValid;
    private String  reservationId;
    private String  prosumerId;
    private String  prosumerName;
    private String  stationName;
    private int     slotNumber;
    private String  scheduledAt;
    private String  status;
    private String  message;

    // Indicates whether the QR code is authentic and the reservation is approved.
    public boolean isValid()          { return isValid; }

    // Returns the unique identifier of the verified reservation.
    public String  getReservationId() { return reservationId; }

    // Returns the identifier of the prosumer associated with the reservation.
    public String  getProsumerId()    { return prosumerId; }

    // Returns the name of the prosumer associated with the reservation.
    public String  getProsumerName()  { return prosumerName; }

    // Returns the name of the solar station associated with the reservation.
    public String  getStationName()   { return stationName; }

    // Returns the reserved energy booking slot number.
    public int     getSlotNumber()    { return slotNumber; }

    // Returns the scheduled date and time of the reservation.
    public String  getScheduledAt()   { return scheduledAt; }

    // Returns the current status of the reservation.
    public String  getStatus()        { return status; }

    // Returns the message describing the QR code verification result.
    public String  getMessage()       { return message; }
}
