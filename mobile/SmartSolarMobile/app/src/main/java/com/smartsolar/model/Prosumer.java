package com.smartsolar.model;

/*
 Represents a prosumer registered in the Smart Solar Microgrid Trading System.
 Stores personal and contact details, account status, and timestamps
 associated with registration, updates, deactivation, and reactivation.
 */
public class Prosumer {

    private String nic;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String deactivationRequestedAt;
    private String deactivatedAt;
    private String reactivatedAt;

    // Returns the prosumer's National Identity Card number.
    public String getNic() { return nic; }

    // Returns the prosumer's name.
    public String getName() { return name; }

    // Returns the prosumer's email address.
    public String getEmail() { return email; }

    // Returns the prosumer's phone number.
    public String getPhone() { return phone; }

    // Returns the prosumer's residential address.
    public String getAddress() { return address; }

    // Returns the current status of the prosumer's account.
    public String getStatus() { return status; }

    // Returns the date and time when the prosumer account was created.
    public String getCreatedAt() { return createdAt; }

    // Returns the date and time when the prosumer account was last updated.
    public String getUpdatedAt() { return updatedAt; }

    // Returns the date and time when account deactivation was requested.
    public String getDeactivationRequestedAt() { return deactivationRequestedAt; }

    // Returns the date and time when the prosumer account was deactivated.
    public String getDeactivatedAt() { return deactivatedAt; }

    // Returns the date and time when the prosumer account was reactivated.
    public String getReactivatedAt() { return reactivatedAt; }
}
