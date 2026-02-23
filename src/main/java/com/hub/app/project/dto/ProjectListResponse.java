package com.hub.app.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectListResponse {
    private UUID projectId;
    private String name;
    private String type;
    private String status;
    private Location location;
    private BigDecimal landArea;
    private LocalDateTime createdAt;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Location {
        private BigDecimal longitude;
        private BigDecimal latitude;
        private String address;
    }
}