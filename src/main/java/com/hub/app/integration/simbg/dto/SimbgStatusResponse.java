package com.hub.app.integration.simbg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

/**
 * DTO for SIMBG application status tracking response.
 */
public class SimbgStatusResponse {

    @JsonProperty("simbgId")
    private String simbgId;

    @JsonProperty("externalReference")
    private String externalReference;

    @JsonProperty("status")
    private String status;

    @JsonProperty("history")
    private List<StatusHistory> history;

    @JsonProperty("assignedOffice")
    private AssignedOffice assignedOffice;

    // Getters and Setters
    public String getSimbgId() {
        return simbgId;
    }

    public void setSimbgId(String simbgId) {
        this.simbgId = simbgId;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<StatusHistory> getHistory() {
        return history;
    }

    public void setHistory(List<StatusHistory> history) {
        this.history = history;
    }

    public AssignedOffice getAssignedOffice() {
        return assignedOffice;
    }

    public void setAssignedOffice(AssignedOffice assignedOffice) {
        this.assignedOffice = assignedOffice;
    }

    /**
     * Inner class for status history.
     */
    public static class StatusHistory {

        @JsonProperty("status")
        private String status;

        @JsonProperty("at")
        private Instant at;

        @JsonProperty("by")
        private String by;

        // Getters and Setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Instant getAt() {
            return at;
        }

        public void setAt(Instant at) {
            this.at = at;
        }

        public String getBy() {
            return by;
        }

        public void setBy(String by) {
            this.by = by;
        }
    }

    /**
     * Inner class for assigned office.
     */
    public static class AssignedOffice {

        @JsonProperty("regionCode")
        private String regionCode;

        @JsonProperty("officeName")
        private String officeName;

        // Getters and Setters
        public String getRegionCode() {
            return regionCode;
        }

        public void setRegionCode(String regionCode) {
            this.regionCode = regionCode;
        }

        public String getOfficeName() {
            return officeName;
        }

        public void setOfficeName(String officeName) {
            this.officeName = officeName;
        }
    }
}