package com.smartsolar.model;

public class UpdateProsumerRequest {

    private final String name;
    private final String email;
    private final String phone;
    private final String address;

    public UpdateProsumerRequest(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}
