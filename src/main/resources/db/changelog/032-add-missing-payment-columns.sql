--liquibase formatted sql

--changeset hub:032-add-missing-payment-columns
-- Add missing columns to payments table: description, due_date, reference_id

ALTER TABLE payments ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS reference_id VARCHAR(255);

--rollback ALTER TABLE payments DROP COLUMN IF EXISTS reference_id;
--rollback ALTER TABLE payments DROP COLUMN IF EXISTS due_date;
--rollback ALTER TABLE payments DROP COLUMN IF EXISTS description;