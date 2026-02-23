--liquibase formatted sql

--changeset hub:22
-- Add project_id column

ALTER TABLE consultations ADD COLUMN IF NOT EXISTS project_id UUID;

--rollback ALTER TABLE users DROP COLUMN IF EXISTS project_id;