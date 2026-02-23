--liquibase formatted sql

--changeset hub:027-update-payments-schema
-- Update payments table to support escrow system

ALTER TABLE payments ADD COLUMN IF NOT EXISTS escrow_account_id UUID;

-- Add foreign key constraint (optional, depending on business rules)
-- ALTER TABLE payments ADD CONSTRAINT fk_payments_escrow_account_id
--     FOREIGN KEY (escrow_account_id) REFERENCES escrow_accounts(id);

-- Update existing payment statuses to include escrow statuses
-- Note: This is handled by the application enum, but we ensure the column can handle the new values

--rollback ALTER TABLE payments DROP COLUMN IF EXISTS escrow_account_id;