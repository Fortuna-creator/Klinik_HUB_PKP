--liquibase formatted sql

--changeset hub:025-add-contract-id-to-consultation-history
ALTER TABLE consultation_history ADD COLUMN contract_id UUID;

CREATE INDEX idx_consultation_history_contract_id ON consultation_history(contract_id);