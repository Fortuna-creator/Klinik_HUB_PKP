package com.hub.app.consultation.model;

import com.hub.app.consultation.dto.PaymentInstallment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "contracts")
@Data @NoArgsConstructor @AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", unique = true)
    private Consultation consultation;

    @Column(name = "contract_value")
    private BigDecimal contractValue;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "terms_conditions_url")
    private String termsConditionsUrl;

    @Column(name = "scope_of_work_url")
    private String scopeOfWorkUrl;

    @Enumerated(EnumType.STRING)
    private ContractStatus status; // DRAFT, REQUEST_FOR_APPROVAL, APPROVED, REJECTED, SIGNED

    @Column(name = "approval_deadline")
    private LocalDateTime approvalDeadline;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "homeowner_signed_at")
    private LocalDateTime homeownerSignedAt;

    @Column(name = "consultant_signed_at")
    private LocalDateTime consultantSignedAt;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Payment installments - stored as JSON
    @Column(name = "payment_installments", columnDefinition = "TEXT")
    @Convert(converter = StringArrayConverter.class)
    private List<PaymentInstallment> installments;

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