package com.hub.app.consultation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "designs")
@Data @NoArgsConstructor @AllArgsConstructor
public class Design {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DesignType type;

    @Enumerated(EnumType.STRING)
    private DesignCategory category;

    private String description;
    private String thumbnailUrl;
    private String fileDedUrl;
    private String fileRabUrl;
    private String fileSpektekUrl;

    @Enumerated(EnumType.STRING)
    private DesignStatus status;

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