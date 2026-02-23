--liquibase formatted sql

--changeset hub:11
-- Update portfolio table to reference consultants instead of users

-- Add new consultant_id column
ALTER TABLE portfolios ADD COLUMN consultant_id BIGINT;

-- Copy data from user_id to consultant_id (assuming consultants are a subset of users)
UPDATE portfolios SET consultant_id = user_id WHERE user_id IS NOT NULL;

-- Add foreign key constraint
ALTER TABLE portfolios ADD CONSTRAINT fk_portfolios_consultant_id
    FOREIGN KEY (consultant_id) REFERENCES consultants(id) ON DELETE CASCADE;

-- Add unique constraint on consultant_id
ALTER TABLE portfolios ADD CONSTRAINT uk_portfolios_consultant_id UNIQUE (consultant_id);

-- Drop old user_id column and constraints
ALTER TABLE portfolios DROP CONSTRAINT portfolios_user_id_key;
ALTER TABLE portfolios DROP CONSTRAINT portfolios_user_id_fkey;
ALTER TABLE portfolios DROP COLUMN user_id;

-- Update indexes
DROP INDEX IF EXISTS idx_portfolios_user_id;
CREATE INDEX IF NOT EXISTS idx_portfolios_consultant_id ON portfolios(consultant_id);

--rollback ALTER TABLE portfolios DROP CONSTRAINT IF EXISTS uk_portfolios_consultant_id;
--rollback ALTER TABLE portfolios DROP CONSTRAINT IF EXISTS fk_portfolios_consultant_id;
--rollback ALTER TABLE portfolios ADD COLUMN user_id BIGINT;
--rollback UPDATE portfolios SET user_id = consultant_id WHERE consultant_id IS NOT NULL;
--rollback ALTER TABLE portfolios ADD CONSTRAINT portfolios_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
--rollback ALTER TABLE portfolios ADD CONSTRAINT portfolios_user_id_key UNIQUE (user_id);
--rollback ALTER TABLE portfolios DROP COLUMN consultant_id;
--rollback DROP INDEX IF EXISTS idx_portfolios_consultant_id;
--rollback CREATE INDEX IF EXISTS idx_portfolios_user_id ON portfolios(user_id);