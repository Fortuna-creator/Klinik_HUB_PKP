package com.hub.app.integration.simbg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for attachment information in SIMBG update requests.
 */
public class AttachmentDto {

    @JsonProperty("docType")
    private String docType;

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