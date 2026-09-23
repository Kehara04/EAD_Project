/*
 * File: CreateReservationRequest.java
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 *
 * Description:
 * Defines the request body used to create an energy reservation.
 * Transfers the selected station, booking slot, scheduled date
 * and time, and optional notes from the Android application
 * to the ASP.NET Core backend API.
 */
package com.smartsolar.model;

public class UpdateProsumerRequest {

    private final String name;
    private final String email;
    private final String phone;
    private final String address;

    // Initializes the required information for creating a new energy reservation.
    public UpdateProsumerRequest(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}
