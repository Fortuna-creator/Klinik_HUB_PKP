--liquibase formatted sql

--changeset hub:018-contract-schema
CREATE TABLE contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL REFERENCES consultations(id) UNIQUE,
    contract_value DECIMAL(15,2) NOT NULL,
    file_url VARCHAR(500),
    terms_conditions_url VARCHAR(500),
    scope_of_work_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    approval_deadline TIMESTAMP,
    approved_at TIMESTAMP,
    signed_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contracts_consultation_id ON contracts(consultation_id);
CREATE INDEX idx_contracts_status ON contracts(status);