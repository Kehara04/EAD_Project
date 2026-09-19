package com.smartsolar.model;

public class RegisterProsumerRequest {

    private String nic;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String password;

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