package com.hub.app.consultation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FavoriteDesignRequest {
    @NotNull
    private UUID designId;

    @NotNull
    private Boolean isFavorited;
}