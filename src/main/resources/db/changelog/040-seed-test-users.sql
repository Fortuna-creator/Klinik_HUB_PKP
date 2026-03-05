--liquibase formatted sql

--changeset hub:040-seed-test-users splitStatements:false

-- =====================================================================
-- Seed data for test users - pre-verified for easier testing
-- All test passwords: "password"
-- All test OTP: "123456"
-- =====================================================================

INSERT INTO users (
    id, email, name, password, phone, address, role, status, activity_status, 
    created_at, updated_at
)
VALUES
    (5001, 'homeowner1@test.com', 'Budi Santoso', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '081234567890', 'Jl. Merdeka No. 123, Jakarta Pusat', 'HOMEOWNER', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5002, 'homeowner2@test.com', 'Siti Nurhaliza', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '081987654321', 'Jl. Sudirman No. 456, Bandung', 'HOMEOWNER', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5003, 'homeowner3@test.com', 'Ahmad Wijaya', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '081456789012', 'Jl. Gatot Subroto No. 789, Surabaya', 'HOMEOWNER', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5004, 'homeowner4@test.com', 'Dewi Lestari', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '082111222333', 'Jl. Malioboro No. 321, Yogyakarta', 'HOMEOWNER', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5005, 'homeowner5@test.com', 'Rany Hermawan', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '082333444555', 'Jl. Pemuda No. 654, Semarang', 'HOMEOWNER', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6001, 'balai.jakarta@test.com', 'Hadi Kurniawan', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '+62-21-3456789', 'Jl. Medan Merdeka Timur No.5, Jakarta Pusat 10110', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6002, 'balai.bandung@test.com', 'Eka Purnama', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '+62-22-7777777', 'Jl. Soekarno Hatta No.456, Bandung 40161', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6003, 'balai.surabaya@test.com', 'Bambang Sutrisno', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '+62-31-333333', 'Jl. Adi Sumarmo No.890, Surabaya 60188', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6004, 'balai.yogya@test.com', 'Nur Cahyo', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '+62-274-555555', 'Jl. Malioboro No.234, Yogyakarta 55271', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6005, 'balai.semarang@test.com', 'Listio Wicaksono', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '+62-24-444444', 'Jl. Pemuda No.567, Semarang 50141', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
