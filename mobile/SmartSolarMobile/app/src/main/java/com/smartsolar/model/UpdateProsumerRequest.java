package com.smartsolar.model;

/*
 Represents the request body used to update a prosumer's profile.
 Stores the prosumer's name, email address, phone number,
 and residential address.
 */

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
