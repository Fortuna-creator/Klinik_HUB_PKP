--liquibase formatted sql

--changeset hub:024-add-file-upload-columns
ALTER TABLE file_uploads ADD COLUMN category VARCHAR(255) NOT NULL;
ALTER TABLE file_uploads ADD COLUMN sub_category VARCHAR(255);
ALTER TABLE file_uploads ADD COLUMN entity_id VARCHAR(255) NOT NULL;