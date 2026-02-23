--liquibase formatted sql

--changeset hub:5
-- Add location fields to users table for consultant location tracking

ALTER TABLE users ADD COLUMN IF NOT EXISTS latitude DECIMAL(10,8);
ALTER TABLE users ADD COLUMN IF NOT EXISTS longitude DECIMAL(11,8);

--rollback ALTER TABLE users DROP COLUMN IF EXISTS longitude;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS latitude;