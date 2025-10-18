--liquibase formatted sql

--changeset hub:6
-- Create consultants table separate from users table

CREATE TABLE IF NOT EXISTS consultants (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    specialty VARCHAR(255),
    rating DECIMAL(3,2),
    location TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_consultants_latitude ON consultants(latitude);
CREATE INDEX IF NOT EXISTS idx_consultants_longitude ON consultants(longitude);

--rollback DROP TABLE IF EXISTS consultants;