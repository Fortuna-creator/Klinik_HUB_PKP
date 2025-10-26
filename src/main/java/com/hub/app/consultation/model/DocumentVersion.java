package com.hub.app.consultation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_versions")
@Data @NoArgsConstructor @AllArgsConstructor
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    @Column(name = "document_type")
    private String documentType; // "DED", "BOQ", "RAB"

    @Column(name = "version")
    private String version; // "1.0", "1.1", "2.0"

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "uploaded_by")
    private String uploadedBy; // "CONSULTANT" or "HOMEOWNER"

    @Column(name = "status")
    private String status; // "DRAFT", "FINAL", "REVISED"

    @Column(name = "notes")
    private String notes;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (uploadedAt == null) {
            uploadedAt = createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}