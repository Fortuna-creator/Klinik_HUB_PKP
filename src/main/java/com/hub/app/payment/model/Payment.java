package com.hub.app.payment.model;

import com.hub.app.consultation.model.Consultation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data @NoArgsConstructor @AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    @Enumerated(EnumType.STRING)
    private PaymentType type; // KONSULTASI, PERIZINAN, KONTRAKTOR, SURVEY, etc.

    private Integer termin;
    private BigDecimal amount;
    private String method;
    private String description; // Deskripsi pembayaran

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String paymentGatewayRef;

    @Column(name = "escrow_account_id")
    private UUID escrowAccountId;

    @Column(name = "due_date")
    private LocalDateTime dueDate; // Tanggal jatuh tempo

    @Column(name = "reference_id")
    private String referenceId; // ID referensi (contract, permit, etc.)

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