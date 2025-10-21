--liquibase formatted sql

--changeset hub:012-survey-schedules-schema
--comment: Create survey_schedules table for consultation survey scheduling

CREATE TABLE survey_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL REFERENCES consultations(id) ON DELETE CASCADE,
    proposed_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'RESCHEDULED', 'COMPLETED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_survey_schedules_consultation_id ON survey_schedules(consultation_id);
CREATE INDEX idx_survey_schedules_status ON survey_schedules(status);
CREATE INDEX idx_survey_schedules_created_at ON survey_schedules(created_at DESC);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_survey_schedules_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_survey_schedules_updated_at
    BEFORE UPDATE ON survey_schedules
    FOR EACH ROW
    EXECUTE FUNCTION update_survey_schedules_updated_at();