package com.smartsolar.model;

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

    public String getNic() { return nic; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getDeactivationRequestedAt() { return deactivationRequestedAt; }
    public String getDeactivatedAt() { return deactivatedAt; }
    public String getReactivatedAt() { return reactivatedAt; }
}
