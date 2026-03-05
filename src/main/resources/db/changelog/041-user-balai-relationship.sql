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
-- Link existing test Balai staff to their respective offices
-- =====================================================================

UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440001' 
  WHERE email = 'balai.jakarta@test.com';  -- Hadi Kurniawan

UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440003' 
  WHERE email = 'balai.bandung@test.com';  -- Eka Purnama

UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440006' 
  WHERE email = 'balai.surabaya@test.com';  -- Bambang Sutrisno

UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440005' 
  WHERE email = 'balai.yogya@test.com';  -- Nur Cahyo

UPDATE users SET balai_id = '550e8400-e29b-41d4-a716-446655440007' 
  WHERE email = 'balai.semarang@test.com';  -- Listio Wicaksono
