package com.hub.app.payment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    private String description;

    @Column(name = "reference_id")
    private String referenceId; // payment/consultation/escrow ID

    @Column(name = "external_reference")
    private String externalReference; // for future gateway integration

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (currency == null) {
            currency = "IDR";
        }
    }
}