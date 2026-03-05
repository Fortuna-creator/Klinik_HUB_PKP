package com.hub.app.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Tracks failed OTP verification attempts to prevent brute force attacks.
 * After 3 failed attempts, user is locked out for 15 minutes.
 */
@Entity
@Table(name = "otp_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer failedAttempts;

    @Column(name = "lockout_expires_at")
    private Instant lockoutExpiresAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        lastAttemptAt = Instant.now();
    }

    public boolean isLockedOut() {
        return lockoutExpiresAt != null && lockoutExpiresAt.isAfter(Instant.now());
    }

    public void recordFailedAttempt() {
        this.failedAttempts++;
        this.lastAttemptAt = Instant.now();
    }

    public void lockOut() {
        this.lockoutExpiresAt = Instant.now().plusSeconds(15 * 60); // 15 minutes
    }

    public void reset() {
        this.failedAttempts = 0;
        this.lockoutExpiresAt = null;
        this.lastAttemptAt = Instant.now();
    }

    public long getRemainingLockoutSeconds() {
        if (lockoutExpiresAt == null) {
            return 0;
        }
        long remainingMillis = lockoutExpiresAt.toEpochMilli() - System.currentTimeMillis();
        return Math.max(0, remainingMillis / 1000); // Convert to seconds, minimum 0
    }
}
