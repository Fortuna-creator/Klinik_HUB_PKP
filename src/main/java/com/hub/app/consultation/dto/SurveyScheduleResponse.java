package com.hub.app.consultation.dto;

import com.hub.app.consultation.model.SurveyStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SurveyScheduleResponse {
    private UUID id;
    private UUID consultationId;
    private Instant proposedDateTime;
    private String notes;
    private SurveyStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}