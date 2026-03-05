package com.hub.app.auth.exception;

import java.time.Instant;

/**
 * Thrown when user exceeds maximum OTP verification attempts
 * and is temporarily locked out.
 */
public class OtpVerificationLockedException extends RuntimeException {
    private final long lockoutExpiresAtMs;

    public OtpVerificationLockedException(String message, Instant lockoutExpiresAt) {
        super(message);
        this.lockoutExpiresAtMs = lockoutExpiresAt.toEpochMilli();
    }

    public long getLockoutExpiresAtMs() {
        return lockoutExpiresAtMs;
    }

    public long getRemainingLockoutSeconds() {
        return (lockoutExpiresAtMs - System.currentTimeMillis()) / 1000;
    }
}
