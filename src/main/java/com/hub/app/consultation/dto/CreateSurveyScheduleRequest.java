package com.hub.app.consultation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateSurveyScheduleRequest {
    @NotNull
    private Instant proposedDateTime;

    private String notes;
}