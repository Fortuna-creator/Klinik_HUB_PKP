package com.hub.app.consultation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "survey_schedules")
@Data @NoArgsConstructor @AllArgsConstructor
public class SurveySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    private Instant proposedDateTime;
    private String notes;

    @Enumerated(EnumType.STRING)
    private SurveyStatus status;

    // Additional fields for enhanced survey management
    private BigDecimal surveyCost;
    private String surveyLocation;
    private Integer estimatedDurationHours;
    private String surveyType; // "ON_SITE", "VIRTUAL", "HYBRID"

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}