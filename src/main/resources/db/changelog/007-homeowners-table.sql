--liquibase formatted sql

--changeset hub:7
-- Create homeowners table separate from users table

CREATE TABLE IF NOT EXISTS homeowners (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    property_type VARCHAR(255),
    property_value DECIMAL(15,2),
    project_needs TEXT,
    ktp_number VARCHAR(255),
    ktp_url VARCHAR(255),
    selfie_url VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'UNVERIFIED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_homeowners_property_type ON homeowners(property_type);

--rollback DROP TABLE IF EXISTS homeowners;