package com.hub.app.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectDetailResponse {
    private UUID projectId;
    private String name;
    private String type;
    private String status;
    private String locationDetail;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal landArea;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ConsultationInfo consultation;
    private PermitInfo permit;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ConsultationInfo {
        private UUID consultationId;
        private String consultantName;
        private LocalDateTime scheduledAt;
        private String status;
        private String notes;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PermitInfo {
        private UUID permitId;
        private String permitType;
        private String status;
        private LocalDateTime submittedAt;
        private String dedDocumentUrl;
        private String pbgDocumentUrl;
    }
}