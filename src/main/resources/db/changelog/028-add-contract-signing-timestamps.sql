-- liquibase formatted sql

-- changeset hub:028-add-contract-signing-timestamps
-- Add individual signing timestamps to contracts table for tracking partial signing

ALTER TABLE contracts
ADD COLUMN homeowner_signed_at TIMESTAMP NULL,
ADD COLUMN consultant_signed_at TIMESTAMP NULL;

-- rollback ALTER TABLE contracts DROP COLUMN homeowner_signed_at, DROP COLUMN consultant_signed_at;