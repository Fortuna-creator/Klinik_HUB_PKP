--liquibase formatted sql

--changeset hub:026-escrow-accounts-schema
-- Escrow Accounts and Transactions Schema for shared account system

CREATE TABLE IF NOT EXISTS escrow_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL REFERENCES consultations(id) ON DELETE CASCADE,
    home_owner_id BIGINT NOT NULL,
    consultant_id BIGINT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    held_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    released_at TIMESTAMP
);

-- Add unique constraint on consultation_id
ALTER TABLE escrow_accounts ADD CONSTRAINT uk_escrow_accounts_consultation_id UNIQUE (consultation_id);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_consultation_id ON escrow_accounts(consultation_id);
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_home_owner_id ON escrow_accounts(home_owner_id);
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_consultant_id ON escrow_accounts(consultant_id);
CREATE INDEX IF NOT EXISTS idx_escrow_accounts_status ON escrow_accounts(status);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    description TEXT,
    reference_id VARCHAR(255),
    external_reference VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for transactions
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_reference_id ON transactions(reference_id);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);

--rollback DROP INDEX IF EXISTS idx_transactions_created_at;
--rollback DROP INDEX IF EXISTS idx_transactions_reference_id;
--rollback DROP INDEX IF EXISTS idx_transactions_type;
--rollback DROP INDEX IF EXISTS idx_transactions_user_id;
--rollback DROP TABLE IF EXISTS transactions;
--rollback DROP INDEX IF EXISTS idx_escrow_accounts_status;
--rollback DROP INDEX IF EXISTS idx_escrow_accounts_consultant_id;
--rollback DROP INDEX IF EXISTS idx_escrow_accounts_home_owner_id;
--rollback DROP INDEX IF EXISTS idx_escrow_accounts_consultation_id;
--rollback ALTER TABLE escrow_accounts DROP CONSTRAINT IF EXISTS uk_escrow_accounts_consultation_id;
--rollback DROP TABLE IF EXISTS escrow_accounts;