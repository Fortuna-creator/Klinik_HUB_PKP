--liquibase formatted sql

--changeset hub:028-offline-consultation-schema
-- Create offline_consultations table
CREATE TABLE IF NOT EXISTS offline_consultations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    balai_id UUID NOT NULL,
    consultation_date DATE NOT NULL,
    consultation_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_offline_consultation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_offline_consultation_balai FOREIGN KEY (balai_id) REFERENCES balai(id) ON DELETE CASCADE,

    -- Check constraint for status enum
    CONSTRAINT chk_offline_consultation_status CHECK (UPPER(status) IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'))
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_offline_consultation_user_id ON offline_consultations(user_id);
CREATE INDEX IF NOT EXISTS idx_offline_consultation_balai_id ON offline_consultations(balai_id);
CREATE INDEX IF NOT EXISTS idx_offline_consultation_date ON offline_consultations(consultation_date);
CREATE INDEX IF NOT EXISTS idx_offline_consultation_status ON offline_consultations(status);

-- Composite index for duplicate booking check
CREATE UNIQUE INDEX IF NOT EXISTS idx_offline_consultation_unique_booking
ON offline_consultations(user_id, balai_id, consultation_date, consultation_time)
WHERE UPPER(status) != 'CANCELLED';

