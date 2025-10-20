package com.hub.app.consultation.dto;

import com.hub.app.consultation.model.DesignCategory;
import com.hub.app.consultation.model.DesignStatus;
import com.hub.app.consultation.model.DesignType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DesignResponse {
    private UUID id;
    private String name;
    private DesignType type;
    private DesignCategory category;
    private String description;
    private String thumbnailUrl;
    private String fileDedUrl;
    private String fileRabUrl;
    private String fileSpektekUrl;
    private DesignStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}