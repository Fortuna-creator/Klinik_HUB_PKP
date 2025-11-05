--liquibase formatted sql

--changeset hub:030-update-escrow-accounts-schema
-- Update Escrow Accounts Schema to support generic payment types

-- Add new columns for generic reference system
ALTER TABLE escrow_accounts ADD COLUMN IF NOT EXISTS reference_type VARCHAR(20);
ALTER TABLE escrow_accounts ADD COLUMN IF NOT EXISTS reference_id UUID;
ALTER TABLE escrow_accounts ADD COLUMN IF NOT EXISTS recipient_id BIGINT;

-- Update existing records to set reference_type for backward compatibility
UPDATE escrow_accounts SET reference_type = 'CONSULTATION' WHERE reference_type IS NULL;
UPDATE escrow_accounts SET reference_id = consultation_id WHERE reference_id IS NULL;
UPDATE escrow_accounts SET recipient_id = consultant_id WHERE recipient_id IS NULL;

-- Make consultation_id nullable for non-consultation payments
ALTER TABLE escrow_accounts ALTER COLUMN consultation_id DROP NOT NULL;

-- Drop the unique constraint on consultation_id since it's now nullable
ALTER TABLE escrow_accounts DROP CONSTRAINT IF EXISTS uk_escrow_accounts_consultation_id;

-- Create new indexes for the generic reference system
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_reference_type ON escrow_accounts(reference_type);
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_reference_id ON escrow_accounts(reference_id);
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_recipient_id ON escrow_accounts(recipient_id);

--rollback DROP INDEX IF EXISTS idx_escrow_accounts_recipient_id;
--rollback DROP INDEX IF EXISTS idx_escrow_accounts_reference_id;
--rollback DROP INDEX IF EXISTS idx_escrow_accounts_reference_type;
--rollback ALTER TABLE escrow_accounts ADD CONSTRAINT uk_escrow_accounts_consultation_id UNIQUE (consultation_id);
--rollback ALTER TABLE escrow_accounts ALTER COLUMN consultation_id SET NOT NULL;
--rollback ALTER TABLE escrow_accounts DROP COLUMN IF EXISTS recipient_id;
--rollback ALTER TABLE escrow_accounts DROP COLUMN IF EXISTS reference_id;
--rollback ALTER TABLE escrow_accounts DROP COLUMN IF EXISTS reference_type;