--liquibase formatted sql

--changeset hub:026-add-file-ids-to-consultation-history
ALTER TABLE consultation_history ADD COLUMN file_ids UUID[];

-- Add comment for documentation
COMMENT ON COLUMN consultation_history.file_ids IS 'Array of file UUIDs for downloadable files, replacing the need to extract IDs from URLs';

-- Create index for better query performance on file_ids
CREATE INDEX idx_consultation_history_file_ids ON consultation_history USING GIN(file_ids);