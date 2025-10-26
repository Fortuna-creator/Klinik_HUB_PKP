--liquibase formatted sql

--changeset hub:020-update-survey-schedule-schema
ALTER TABLE survey_schedules
ADD COLUMN survey_cost DECIMAL(15,2),
ADD COLUMN survey_location VARCHAR(255),
ADD COLUMN estimated_duration_hours INTEGER,
ADD COLUMN survey_type VARCHAR(50) DEFAULT 'ON_SITE';

CREATE INDEX IF NOT EXISTS idx_survey_schedules_status ON survey_schedules(status);
CREATE INDEX IF NOT EXISTS idx_survey_schedules_created_at ON survey_schedules(created_at DESC);