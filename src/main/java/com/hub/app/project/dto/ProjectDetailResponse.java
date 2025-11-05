package com.hub.app.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        private String consultationType;
        private List<ConsultationStep> consultationHistory;
        private DocumentExchangeInfo documentExchange;
        private List<RevisionRequest> revisionRequests;
        private List<SurveyScheduleInfo> surveySchedules;
        private SurveyScheduleInfo currentSurveySchedule;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ConsultationStep {
        private String step;
        private String state;
        private String title;
        private String subtitle;
        private List<String> files;  // Now contains fileId strings instead of URLs
        private List<String> attachments;
        private ConsultationMetadata metadata;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ConsultationMetadata {
        private LocalDateTime dateTime;
        private String actor;
        private String status;
        private String notes;
        private BigDecimal surveyCost;
        private String surveyLocation;
        private UUID contractId;
        private UUID scheduleId;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DocumentExchangeInfo {
        private DocumentVersion currentDed;
        private DocumentVersion currentBoq;
        private DocumentVersion currentRab;
        private List<DocumentExchange> exchangeHistory;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DocumentVersion {
        private String documentType;
        private String version;
        private String fileUrl;
        private LocalDateTime uploadedAt;
        private String uploadedBy;
        private String status;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DocumentExchange {
        private String documentType;
        private String action;
        private String version;
        private String fileUrl;
        private LocalDateTime timestamp;
        private String actor;
        private String notes;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RevisionRequest {
        private UUID revisionId;
        private String title;
        private String description;
        private String priority;
        private String status;
        private LocalDateTime requestedAt;
        private LocalDateTime completedAt;
        private String requestedBy;
        private List<String> affectedDocuments;
        private String resolution;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SurveyScheduleInfo {
        private UUID scheduleId;
        private LocalDateTime proposedDateTime;
        private String status;
        private String notes;
        private String proposedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private BigDecimal surveyCost;
        private String surveyLocation;
        private Integer estimatedDurationHours;
        private String surveyType;
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