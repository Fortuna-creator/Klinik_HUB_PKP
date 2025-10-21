package com.hub.app.consultation.dto;

import com.hub.app.consultation.model.FinalDocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class FinalDocumentResponse {
    private UUID id;
    private UUID consultationId;
    private String fileDedUrl;
    private String fileRabUrl;
    private String fileSpektekUrl;
    private FinalDocumentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}