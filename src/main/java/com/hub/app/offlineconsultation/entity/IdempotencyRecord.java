package com.hub.app.offlineconsultation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Stores idempotency metadata for state-changing requests.
 * Records are automatically expired via a scheduled cleanup job
 * that deletes entries whose {@code ttl} has passed.
 */
@Entity
@Table(name = "oc_idempotency_store", indexes = {
        @Index(name = "idx_oc_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_oc_idempotency_ttl", columnList = "ttl")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "endpoint", length = 255)
    private String endpoint;

    @Column(name = "request_hash", length = 255)
    private String requestHash;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IdempotencyStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Time-to-live — record should be purged after this instant. */
    @Column(name = "ttl", nullable = false)
    private Instant ttl;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (ttl == null) {
            ttl = Instant.now().plus(24, ChronoUnit.HOURS);
        }
    }
}
