package com.hub.app.integration.simbg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for document information in SIMBG requests.
 */
public class DocumentDto {

    @JsonProperty("docType")
    private String docType; // site_plan, gambar_teknis, etc.

    @JsonProperty("fileUrl")
    private String fileUrl;

    // Getters and Setters
    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}