package com.hub.app.consultation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "consultation_history")
@Data @NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "consultation")
public class ConsultationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    @Column(name = "step")
    private String step; // "CONSULTATION", "SURVEY", "CONTRACT", "DOCUMENT"

    @Column(name = "state")
    private String state; // "CREATED", "ACCEPTED", "SCHEDULED", etc.

    @Column(name = "title")
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "files", columnDefinition = "TEXT[]")
    private List<String> files;

    @Column(name = "attachments", columnDefinition = "TEXT[]")
    private List<String> attachments;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "actor")
    private String actor; // "HOMEOWNER", "CONSULTANT", "SYSTEM"

    @Column(name = "status")
    private String status;

    @Column(name = "notes")
    private String notes;

    // Additional fields for specific events
    @Column(name = "survey_cost")
    private BigDecimal surveyCost;

    @Column(name = "survey_location")
    private String surveyLocation;

    @Column(name = "schedule_id")
    private UUID scheduleId;

    @Column(name = "contract_id")
    private UUID contractId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (dateTime == null) {
            dateTime = createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}