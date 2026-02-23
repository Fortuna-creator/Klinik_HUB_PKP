package com.hub.app.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateProjectResponse {
    private UUID projectId;
    private String name;
    private String type;
    private LocalDateTime createdAt;
}