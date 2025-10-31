--liquibase formatted sql

--changeset hub:023-add-schedule-id-to-consultation-history
ALTER TABLE consultation_history ADD COLUMN schedule_id UUID;