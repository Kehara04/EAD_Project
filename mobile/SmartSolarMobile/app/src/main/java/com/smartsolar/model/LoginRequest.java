package com.smartsolar.model;

/*
 Represents the login request sent to the authentication API.
 Stores the user's email address and password for authentication.
 */
public class LoginRequest {

    private String email;
    private String password;

    // Initializes the login request with the user's email address and password.
    public LoginRequest(
            String email,
            String password) {

        this.email = email;
        this.password = password;
    }
}