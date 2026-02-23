package com.hub.app.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID projectId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, length = 255)
    private String locationDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type; // PROTOTYPE, NON_PROTOTYPE

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal landArea;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal income;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status; // ACTIVE, COMPLETED, CANCELLED

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private com.hub.app.user.model.User user;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (projectId == null) {
            projectId = UUID.randomUUID();
        }
        if (status == null) {
            status = ProjectStatus.CREATED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}