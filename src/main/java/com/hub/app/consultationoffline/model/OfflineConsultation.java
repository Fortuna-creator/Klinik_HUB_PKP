package com.hub.app.consultationoffline.model;

import com.hub.app.balaioffline.model.Balai;
import com.hub.app.consultationoffline.model.ConsultationStatus;
import com.hub.app.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "offline_consultations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balai_id", nullable = false)
    private Balai balai;

    @Column(name = "consultation_date", nullable = false)
    private LocalDate consultationDate;

    @Column(name = "consultation_time", nullable = false)
    private LocalTime consultationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConsultationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) {
            status = ConsultationStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

