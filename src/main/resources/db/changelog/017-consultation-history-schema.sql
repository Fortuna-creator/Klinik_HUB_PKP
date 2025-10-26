--liquibase formatted sql

--changeset hub:017-consultation-history-schema
CREATE TABLE consultation_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL REFERENCES consultations(id),
    step VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(500),
    files TEXT[],
    attachments TEXT[],
    date_time TIMESTAMP NOT NULL,
    actor VARCHAR(50) NOT NULL,
    status VARCHAR(50),
    notes TEXT,
    survey_cost DECIMAL(15,2),
    survey_location VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_consultation_history_consultation_id ON consultation_history(consultation_id);
CREATE INDEX idx_consultation_history_date_time ON consultation_history(date_time DESC);
CREATE INDEX idx_consultation_history_step ON consultation_history(step);
CREATE INDEX idx_consultation_history_state ON consultation_history(state);