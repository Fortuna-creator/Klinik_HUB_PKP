package com.hub.app.integration.simbg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for SIMBG application update request.
 */
public class SimbgUpdateRequest {

    @JsonProperty("comments")
    private String comments;

    @JsonProperty("attachments")
    private List<AttachmentDto> attachments;

    // Getters and Setters
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<AttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDto> attachments) {
        this.attachments = attachments;
    }
}