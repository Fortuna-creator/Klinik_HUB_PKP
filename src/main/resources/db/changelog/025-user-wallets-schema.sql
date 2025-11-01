--liquibase formatted sql

--changeset hub:025-user-wallets-schema
-- User Wallets Schema for escrow account system

CREATE TABLE IF NOT EXISTS user_wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add unique constraint on user_id
ALTER TABLE user_wallets ADD CONSTRAINT uk_user_wallets_user_id UNIQUE (user_id);

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_user_wallets_user_id ON user_wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_user_wallets_active ON user_wallets(user_id, is_active);

--rollback DROP INDEX IF EXISTS idx_user_wallets_active;
--rollback DROP INDEX IF EXISTS idx_user_wallets_user_id;
--rollback ALTER TABLE user_wallets DROP CONSTRAINT IF EXISTS uk_user_wallets_user_id;
--rollback DROP TABLE IF EXISTS user_wallets;