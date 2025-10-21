package com.hub.app.integration.simbg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * DTO for SIMBG document retrieval response.
 */
public class SimbgDocumentResponse {

    @JsonProperty("docId")
    private String docId;

    @JsonProperty("type")
    private String type; // PBG, SLF, etc.

    @JsonProperty("issuedAt")
    private LocalDate issuedAt;

    @JsonProperty("downloadUrl")
    private String downloadUrl;

    @JsonProperty("checksum")
    private String checksum;

    // Getters and Setters
    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDate issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}