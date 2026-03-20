--liquibase formatted sql

--changeset hub:041-user-balai-relationship

-- =====================================================================
-- Add balai_id column to users table to link Balai staff to their office
-- =====================================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS balai_id UUID;

ALTER TABLE users ADD CONSTRAINT fk_user_balai 
    FOREIGN KEY (balai_id) 
    REFERENCES balai(id) 
    ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_user_balai_id ON users(balai_id);

-- =====================================================================
-- Link test Balai staff to their respective offices (19 regional offices)
-- =====================================================================

-- SUMATERA REGION (5)
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440001' WHERE email = 'bp2p.sumatera1@pu.go.id';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440002' WHERE email = 'bp2p.sumatera2@pu.go.id';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440003' WHERE email = 'bp2p.sumatera3@pu.go.id';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440004' WHERE email = 'bp2p.sumatera4@pu.go.id';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440005' WHERE email = 'bp2p.sumatera5@pu.go.id';

-- JAWA REGION (4)
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440006' WHERE email = 'bp2p.jawa1@pu.go.id';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440007' WHERE email = 'bp2p.jawa2@pu.go.id';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440008' WHERE email = 'bp2p.jawa3@pu.go.id';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440009' WHERE email = 'bp2p.jawa4@pu.go.id';

-- KALIMANTAN REGION (2)
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440010' WHERE email = 'bp2p.kalimantan1@pu.go.id';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440011' WHERE email = 'bp2pkalimantan2@gmail.com';

-- SULAWESI REGION (3)
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440012' WHERE email = 'balaipnpsul1@gmail.com';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440013' WHERE email = 'bp2p.sulawesill@gmail.com';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440014' WHERE email = 'bp2p.sulawesi3@pu.go.id';

-- NUSA TENGGARA REGION (2)
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440015' WHERE email = 'bp2p.nusatenggara1@pu.go.id';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440016' WHERE email = 'bp2p.nusatenggara2@pu.go.id';

-- MALUKU REGION (1)
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440017' WHERE email = 'bp3kp.maluku@pkp.go.id';

-- PAPUA REGION (2)
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440018' WHERE email = 'bp2p.papua1@pu.go.id';
UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440019' WHERE email = 'bp2p.papua2@pu.go.id';
