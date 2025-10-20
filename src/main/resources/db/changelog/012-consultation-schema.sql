--liquibase formatted sql

--changeset hub:12
-- Consultation Module Schema

-- Create designs table
CREATE TABLE IF NOT EXISTS designs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(500),
    file_ded_url VARCHAR(500),
    file_rab_url VARCHAR(500),
    file_spektek_url VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create consultations table
CREATE TABLE IF NOT EXISTS consultations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    home_owner_id BIGINT REFERENCES users(id),
    consultant_id BIGINT REFERENCES users(id),
    design_id UUID REFERENCES designs(id),
    consultation_type VARCHAR(50) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create revisions table
CREATE TABLE IF NOT EXISTS revisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID REFERENCES consultations(id),
    version VARCHAR(50) NOT NULL,
    notes TEXT,
    file_url VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    revision_count INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create contracts table
CREATE TABLE IF NOT EXISTS contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID REFERENCES consultations(id),
    contract_data JSONB,
    pdf_url VARCHAR(500),
    digital_signature BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID REFERENCES consultations(id),
    termin INTEGER NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    method VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    payment_gateway_ref VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create final_documents table
CREATE TABLE IF NOT EXISTS final_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID REFERENCES consultations(id),
    file_ded_url VARCHAR(500),
    file_rab_url VARCHAR(500),
    file_spektek_url VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create favorite_designs table
CREATE TABLE IF NOT EXISTS favorite_designs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    home_owner_id BIGINT REFERENCES users(id),
    design_id UUID REFERENCES designs(id),
    is_favorited BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_designs_type_status ON designs(type, status);
CREATE INDEX IF NOT EXISTS idx_consultations_home_owner ON consultations(home_owner_id);
CREATE INDEX IF NOT EXISTS idx_consultations_consultant ON consultations(consultant_id);
CREATE INDEX IF NOT EXISTS idx_consultations_status ON consultations(status);
CREATE INDEX IF NOT EXISTS idx_revisions_consultation ON revisions(consultation_id);
CREATE INDEX IF NOT EXISTS idx_contracts_consultation_status ON contracts(consultation_id, status);
CREATE INDEX IF NOT EXISTS idx_payments_consultation ON payments(consultation_id);
CREATE INDEX IF NOT EXISTS idx_final_documents_consultation ON final_documents(consultation_id);
CREATE INDEX IF NOT EXISTS idx_favorite_designs_home_owner ON favorite_designs(home_owner_id);
CREATE INDEX IF NOT EXISTS idx_favorite_designs_design ON favorite_designs(design_id);