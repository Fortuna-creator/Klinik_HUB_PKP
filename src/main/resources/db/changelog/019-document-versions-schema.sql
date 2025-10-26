--liquibase formatted sql

--changeset hub:019-document-versions-schema
CREATE TABLE document_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL REFERENCES consultations(id),
    document_type VARCHAR(10) NOT NULL CHECK (document_type IN ('DED', 'BOQ', 'RAB')),
    version VARCHAR(20) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    uploaded_by VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
    notes TEXT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_document_versions_consultation_id ON document_versions(consultation_id);
CREATE INDEX idx_document_versions_document_type ON document_versions(document_type);
CREATE INDEX idx_document_versions_status ON document_versions(status);
CREATE INDEX idx_document_versions_uploaded_at ON document_versions(uploaded_at DESC);