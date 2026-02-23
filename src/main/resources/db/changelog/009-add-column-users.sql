--liquibase formatted sql

--changeset hub:9
-- Add new columns to users table

ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio VARCHAR(255);

--rollback ALTER TABLE consultants DROP COLUMN IF EXISTS avatar_url;
--rollback ALTER TABLE consultants DROP COLUMN IF EXISTS bio;