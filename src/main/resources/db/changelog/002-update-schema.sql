--liquibase formatted sql

--changeset hub:2
-- Update schema to match current models

-- Update users table
ALTER TABLE users DROP COLUMN IF EXISTS username;
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS ktp_number VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS ktp_url VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS selfie_url VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS activity_status VARCHAR(50);

-- Update otp_tokens table
ALTER TABLE otp_tokens DROP COLUMN IF EXISTS created_at;
ALTER TABLE otp_tokens RENAME COLUMN token TO code;
ALTER TABLE otp_tokens ADD COLUMN IF NOT EXISTS used BOOLEAN DEFAULT FALSE;

--rollback ALTER TABLE otp_tokens DROP COLUMN IF EXISTS used;
--rollback ALTER TABLE otp_tokens RENAME COLUMN code TO token;
--rollback ALTER TABLE otp_tokens ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS activity_status;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS selfie_url;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS ktp_url;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS ktp_number;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS address;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS phone;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS name;
--rollback ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(255) UNIQUE;