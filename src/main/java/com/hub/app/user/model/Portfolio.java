package com.hub.app.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "portfolios")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Portfolio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // Basic Information
    private String bio;
    private String specialization;
    private String experience; // years of experience
    private String education;

    // Certifications
    @Column(length = 2000)
    private String certifications;

    // Contact Information
    private String linkedinUrl;
    private String websiteUrl;

    // Portfolio Projects
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioProject> projects;

    // Portfolio Status
    @Enumerated(EnumType.STRING)
    private PortfolioStatus status; // DRAFT, COMPLETED, INCOMPLETE

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