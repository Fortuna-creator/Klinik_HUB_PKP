package com.hub.app.offlineconsultation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Balai (office) entity — moved into the offlineconsultation module for
 * package isolation. Maps to the existing {@code balai} table with additional
 * columns required by the API contract (location, working hours).
 */
@Entity(name = "BalaiEntity")
@Table(name = "balai")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalaiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "web_url", length = 255)
    private String webUrl;

    @Column(name = "working_hours_start", length = 10)
    private String workingHoursStart;

    @Column(name = "working_hours_end", length = 10)
    private String workingHoursEnd;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
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
