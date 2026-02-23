--liquibase formatted sql

--changeset hub:033-add-missing-project-type-column
-- Add missing type column to projects table

ALTER TABLE projects ADD COLUMN IF NOT EXISTS type VARCHAR(50) NOT NULL DEFAULT 'PROTOTYPE';

--rollback ALTER TABLE projects DROP COLUMN IF EXISTS type;