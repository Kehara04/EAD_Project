package com.smartsolar.model;

/**
 * Represents the request body used to register a new prosumer.
 * Stores the prosumer's identification details, contact information,
 * residential address, and password for account registration.
 */
public class RegisterProsumerRequest {

    private String nic;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String password;

    // Initializes the registration request with the prosumer's personal details and credentials.
    public RegisterProsumerRequest(
            String nic,
            String name,
            String email,
            String phone,
            String address,
            String password) {

        this.nic = nic;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
    }
}