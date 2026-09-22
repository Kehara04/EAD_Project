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

    public String getReservationId() { return reservationId; }
    public String getProsumerId()    { return prosumerId; }
    public String getStationId()     { return stationId; }
    public int    getSlotNumber()    { return slotNumber; }
    public String getScheduledAt()   { return scheduledAt; }
    public String getIssuedAt()      { return issuedAt; }
    public String getSignature()     { return signature; }
}
