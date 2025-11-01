package com.hub.app.wallet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

import com.hub.app.user.model.User;

@Entity
@Table(name = "user_wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(length = 3)
    private String currency;

    @Column(name = "is_active")
    private Boolean isActive;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (currency == null) {
            currency = "IDR";
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}