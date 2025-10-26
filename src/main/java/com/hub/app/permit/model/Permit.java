package com.hub.app.permit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "permits")
@Data @NoArgsConstructor @AllArgsConstructor
public class Permit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "simbg_id")
    private String simbgId;

    @Column(name = "permit_type")
    private String permitType;

    @Enumerated(EnumType.STRING)
    private PermitStatus status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "ded_document_url")
    private String dedDocumentUrl;

    @Column(name = "pbg_document_url")
    private String pbgDocumentUrl;

    @Column(name = "notes")
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}