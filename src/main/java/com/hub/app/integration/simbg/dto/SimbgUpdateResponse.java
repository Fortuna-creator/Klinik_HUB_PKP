package com.hub.app.integration.simbg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for SIMBG application update response.
 */
public class SimbgUpdateResponse {

    @JsonProperty("simbgId")
    private String simbgId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}