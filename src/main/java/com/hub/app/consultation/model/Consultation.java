package com.hub.app.consultation.model;

import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.HomeOwner;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consultations")
@Data @NoArgsConstructor @AllArgsConstructor
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_owner_id")
    private HomeOwner homeOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultant_id")
    private Consultant consultant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "design_id")
    private Design design;

    @Enumerated(EnumType.STRING)
    private ConsultationType consultationType;

    @Enumerated(EnumType.STRING)
    private ConsultationChannel channel;

    @Enumerated(EnumType.STRING)
    private ConsultationStatus status;

    private Instant startedAt;
    private Instant completedAt;

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