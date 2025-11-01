package com.hub.app.escrow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.hub.app.consultation.model.Consultation;

@Entity
@Table(name = "escrow_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscrowAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", unique = true)
    private Consultation consultation;

    @Column(name = "home_owner_id")
    private Long homeOwnerId;

    @Column(name = "consultant_id")
    private Long consultantId;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal heldAmount;

    @Enumerated(EnumType.STRING)
    private EscrowStatus status;

    private String description;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant releasedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (heldAmount == null) {
            heldAmount = BigDecimal.ZERO;
        }
        if (status == null) {
            status = EscrowStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}