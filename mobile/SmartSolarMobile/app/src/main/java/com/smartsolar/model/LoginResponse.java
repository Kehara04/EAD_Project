package com.smartsolar.model;

/*
 Represents the response returned by the authentication API after login.
 Stores the authentication token, user details, role, and reference identifier.
 */
public class LoginResponse {

    private String token;
    private String userId;
    private String name;
    private String email;
    private String role;
    private String referenceId;

    // Returns the authentication token used for authorized API requests.
    public String getToken() {
        return token;
    }

    // Returns the unique identifier of the authenticated user.
    public String getUserId() {
        return userId;
    }

    // Returns the name of the authenticated user.
    public String getName() {
        return name;
    }

    // Returns the email address of the authenticated user.
    public String getEmail() {
        return email;
    }

    // Returns the assigned role of the authenticated user.
    public String getRole() {
        return role;
    }

    // Returns the reference identifier associated with the user.
    public String getReferenceId() {
        return referenceId;
    }
}