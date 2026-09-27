package com.smartsolar.model;

/**
 * Mirrors the QrPayload DTO from the C# Web API.
 * Serialised to/from JSON by Gson via Retrofit.
 * The signature field is an HMAC-SHA256 hex digest
 * computed on the server and re-verified on every scan.
 */
public class QrPayload {

    private String reservationId;
    private String prosumerId;
    private String stationId;
    private int    slotNumber;
    private String scheduledAt;
    private String issuedAt;
    private String signature;

    // Returns the unique identifier of the reservation.
    public String getReservationId() { return reservationId; }

    // Returns the identifier of the prosumer associated with the reservation.
    public String getProsumerId()    { return prosumerId; }

    // Returns the identifier of the solar station.
    public String getStationId()     { return stationId; }

    // Returns the reserved energy booking slot number.
    public int    getSlotNumber()    { return slotNumber; }

    // Returns the scheduled date and time of the reservation.
    public String getScheduledAt()   { return scheduledAt; }

    // Returns the date and time when the QR payload was issued.
    public String getIssuedAt()      { return issuedAt; }

    // Returns the server-generated HMAC-SHA256 signature used for verification.
    public String getSignature()     { return signature; }
}
