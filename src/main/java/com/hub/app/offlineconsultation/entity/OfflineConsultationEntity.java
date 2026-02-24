package com.hub.app.offlineconsultation.entity;

import com.hub.app.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single offline consultation booking between a Warga (citizen)
 * and a Balai (office). Status follows a strict state machine enforced by
 * {@link ConsultationStatus#canTransitionTo(ConsultationStatus)}.
 */
@Entity
@Table(name = "oc_offline_consultations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineConsultationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balai_id", nullable = false)
    private BalaiEntity balai;

    @Column(name = "consultation_date", nullable = false)
    private LocalDate consultationDate;

    /** Time slot in display format, e.g. "09.00 - 10.00" */
    @Column(name = "time_slot", nullable = false, length = 30)
    private String timeSlot;

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Convert(converter = ConsultationStatusConverter.class)
    @Column(name = "status", nullable = false, length = 30)
    private ConsultationStatus status;

    /** Verification code — set when consultation is completed by Balai. */
    @Column(name = "consultation_id", length = 50)
    private String consultationId;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) {
            status = ConsultationStatus.MENUNGGU_KONFIRMASI;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
