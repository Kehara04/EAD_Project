package com.smartsolar.model;

public class LoginResponse {

    private String token;
    private String userId;
    private String name;
    private String email;
    private String role;
    private String referenceId;

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getReferenceId() {
        return referenceId;
    }
}