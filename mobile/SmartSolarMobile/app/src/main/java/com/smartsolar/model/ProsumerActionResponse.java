package com.smartsolar.model;

/*
 Represents the response returned after performing an action on a prosumer account.
 Contains a response message and the associated prosumer information.
 */
public class ProsumerActionResponse {

    private String message;
    private Prosumer prosumer;

    // Returns the message describing the result of the prosumer account action.
    public String getMessage() {
        return message;
    }

    // Returns the prosumer information associated with the action.
    public Prosumer getProsumer() {
        return prosumer;
    }
}
