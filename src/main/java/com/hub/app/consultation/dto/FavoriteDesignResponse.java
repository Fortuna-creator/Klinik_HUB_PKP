package com.hub.app.consultation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class FavoriteDesignResponse {
    private UUID id;
    private Long homeOwnerId;
    private UUID designId;
    private Boolean isFavorited;
    private Instant createdAt;
}