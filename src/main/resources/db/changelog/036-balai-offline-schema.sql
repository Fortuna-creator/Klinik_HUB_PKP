--liquibase formatted sql

--changeset hub:024-balai-schema
-- Create balai table
CREATE TABLE IF NOT EXISTS balai (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    web_url VARCHAR(255),
    email VARCHAR(100),
    phone_number VARCHAR(20),
    image_url VARCHAR(255),
    address VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster lookups by name
CREATE INDEX IF NOT EXISTS idx_balai_name ON balai(name);

