--liquibase formatted sql

--changeset hub:038-oc-schema splitStatements:true

-- =====================================================================
-- Offline Consultation Scheduling module (offlineconsultation package)
-- =====================================================================

-- 1. Add contract-required columns to existing balai table
ALTER TABLE balai ADD COLUMN IF NOT EXISTS location VARCHAR(255);
ALTER TABLE balai ADD COLUMN IF NOT EXISTS working_hours_start VARCHAR(10);
ALTER TABLE balai ADD COLUMN IF NOT EXISTS working_hours_end VARCHAR(10);

-- 2. Offline consultation bookings
CREATE TABLE IF NOT EXISTS oc_offline_consultations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             BIGINT       NOT NULL,
    balai_id            UUID         NOT NULL,
    consultation_date   DATE         NOT NULL,
    time_slot           VARCHAR(30)  NOT NULL,
    topic               VARCHAR(255) NOT NULL,
    description         TEXT,
    status              VARCHAR(30)  NOT NULL DEFAULT 'menunggu_konfirmasi',
    consultation_id     VARCHAR(50),
    cancellation_reason TEXT,
    notes               TEXT,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_oc_consultation_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_oc_consultation_balai
        FOREIGN KEY (balai_id) REFERENCES balai(id) ON DELETE CASCADE,

    CONSTRAINT chk_oc_consultation_status
        CHECK (status IN ('menunggu_konfirmasi','terkonfirmasi','sedang_berjalan','selesai','dibatalkan'))
);

CREATE INDEX IF NOT EXISTS idx_oc_consultation_user_id  ON oc_offline_consultations(user_id);
CREATE INDEX IF NOT EXISTS idx_oc_consultation_balai_id ON oc_offline_consultations(balai_id);
CREATE INDEX IF NOT EXISTS idx_oc_consultation_date     ON oc_offline_consultations(consultation_date);
CREATE INDEX IF NOT EXISTS idx_oc_consultation_status   ON oc_offline_consultations(status);

-- 3. Idempotency store
CREATE TABLE IF NOT EXISTS oc_idempotency_store (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
    user_id          BIGINT,
    endpoint         VARCHAR(255),
    request_hash     VARCHAR(255),
    response_body    TEXT,
    http_status      INTEGER,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PROCESSING',
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ttl              TIMESTAMP    NOT NULL,

    CONSTRAINT chk_oc_idempotency_status
        CHECK (status IN ('PROCESSING','SUCCESS','FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_oc_idempotency_key ON oc_idempotency_store(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_oc_idempotency_ttl ON oc_idempotency_store(ttl);
