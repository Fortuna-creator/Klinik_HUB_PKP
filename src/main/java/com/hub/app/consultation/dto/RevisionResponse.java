package com.hub.app.consultation.dto;

import com.hub.app.consultation.model.RevisionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RevisionResponse {
    private UUID id;
    private UUID consultationId;
    private String version;
    private String notes;
    private String fileUrl;
    private RevisionStatus status;
    private Integer revisionCount;
    private Instant createdAt;
    private Instant updatedAt;
}