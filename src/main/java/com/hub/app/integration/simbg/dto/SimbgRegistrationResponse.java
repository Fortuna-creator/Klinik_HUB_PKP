package com.hub.app.integration.simbg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for SIMBG application registration response.
 */
public class SimbgRegistrationResponse {

    @JsonProperty("simbgId")
    private String simbgId;

    @JsonProperty("status")
    private String status; // SUBMITTED

    // Getters and Setters
    public String getSimbgId() {
        return simbgId;
    }

    public void setSimbgId(String simbgId) {
        this.simbgId = simbgId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}