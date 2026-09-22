package com.smartsolar.model;

/**
 * Mirrors VerifyQrResponse from the C# Web API.
 * Populated from the POST /api/operator/verify response.
 * isValid = true means the QR is authentic and the reservation is Approved.
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

    public boolean isValid()          { return isValid; }
    public String  getReservationId() { return reservationId; }
    public String  getProsumerId()    { return prosumerId; }
    public String  getProsumerName()  { return prosumerName; }
    public String  getStationName()   { return stationName; }
    public int     getSlotNumber()    { return slotNumber; }
    public String  getScheduledAt()   { return scheduledAt; }
    public String  getStatus()        { return status; }
    public String  getMessage()       { return message; }
}
