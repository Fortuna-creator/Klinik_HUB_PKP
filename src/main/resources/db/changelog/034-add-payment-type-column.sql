--liquibase formatted sql

--changeset hub:034-add-payment-type-column
-- Add missing type column to payments table

ALTER TABLE payments ADD COLUMN IF NOT EXISTS type VARCHAR(50);

--rollback ALTER TABLE payments DROP COLUMN IF EXISTS type;