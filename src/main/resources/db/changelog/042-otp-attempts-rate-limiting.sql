-- Migration: Create OTP Attempts Table
-- Purpose: Track failed OTP verification attempts for brute force protection
-- Rate limiting: 3 failed attempts per user locks out for 15 minutes

CREATE TABLE IF NOT EXISTS otp_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    lockout_expires_at TIMESTAMP WITHOUT TIME ZONE NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_attempt_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_otp_attempts_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_otp_attempts_lockout_expires_at ON otp_attempts (lockout_expires_at);
CREATE INDEX IF NOT EXISTS idx_otp_attempts_last_attempt_at ON otp_attempts (last_attempt_at);
