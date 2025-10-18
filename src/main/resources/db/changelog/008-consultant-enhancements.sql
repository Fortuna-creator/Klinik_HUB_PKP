--liquibase formatted sql

--changeset hub:8
-- Add new columns to consultants table for enhanced consultant information

ALTER TABLE consultants ADD COLUMN IF NOT EXISTS type VARCHAR(20) DEFAULT 'PROFESSIONAL';
ALTER TABLE consultants ADD COLUMN IF NOT EXISTS experience_level VARCHAR(50);
ALTER TABLE consultants ADD COLUMN IF NOT EXISTS availability_status VARCHAR(20) DEFAULT 'AVAILABLE';
ALTER TABLE consultants ADD COLUMN IF NOT EXISTS contact_info TEXT;
ALTER TABLE consultants ADD COLUMN IF NOT EXISTS hourly_rate DECIMAL(10,2);
ALTER TABLE consultants ADD COLUMN IF NOT EXISTS package_cost DECIMAL(10,2);

-- Add comments for documentation
COMMENT ON COLUMN consultants.type IS 'Type of consultant: PROFESSIONAL or STUDENT';
COMMENT ON COLUMN consultants.experience_level IS 'Experience level (e.g., Beginner, Intermediate, Expert)';
COMMENT ON COLUMN consultants.availability_status IS 'Current availability: AVAILABLE, BUSY, UNAVAILABLE';
COMMENT ON COLUMN consultants.contact_info IS 'Additional contact information';
COMMENT ON COLUMN consultants.hourly_rate IS 'Hourly rate for professional consultants';
COMMENT ON COLUMN consultants.package_cost IS 'Package cost for professional consultants';

--rollback ALTER TABLE consultants DROP COLUMN IF EXISTS type;
--rollback ALTER TABLE consultants DROP COLUMN IF EXISTS experience_level;
--rollback ALTER TABLE consultants DROP COLUMN IF EXISTS availability_status;
--rollback ALTER TABLE consultants DROP COLUMN IF EXISTS contact_info;
--rollback ALTER TABLE consultants DROP COLUMN IF EXISTS hourly_rate;
--rollback ALTER TABLE consultants DROP COLUMN IF EXISTS package_cost;