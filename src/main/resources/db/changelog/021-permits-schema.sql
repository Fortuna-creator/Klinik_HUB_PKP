-- Create permits table
CREATE TABLE permits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    simbg_id VARCHAR(255),
    permit_type VARCHAR(100) NOT NULL DEFAULT 'BUILDING_PERMIT',
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    submitted_at TIMESTAMP,
    approved_at TIMESTAMP,
    ded_document_url TEXT,
    pbg_document_url TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_permits_project FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE
);

-- Create index for faster lookups
CREATE INDEX idx_permits_project_id ON permits(project_id);
CREATE INDEX idx_permits_status ON permits(status);