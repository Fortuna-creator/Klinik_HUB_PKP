--liquibase formatted sql

--changeset hub:039-seed-balai-data splitStatements:false

-- =====================================================================
-- Seed data for Balai (Office) table
-- These are realistic government office locations for offline consultations
-- =====================================================================

INSERT INTO balai (id, name, location, description, email, phone_number, address, image_url, web_url, working_hours_start, working_hours_end, created_at, updated_at)
VALUES
    -- Jakarta Region
    (
        '550e8400-e29b-41d4-a716-446655440001',
        'Balai Permukiman dan Perumahan Jakarta',
        'Jakarta Pusat',
        'Kantor pusat untuk layanan konsultasi permukiman dan perumahan di wilayah Jakarta',
        'info@balai-jakarta.go.id',
        '+62-21-3456789',
        'Jl. Medan Merdeka Timur No.5, Jakarta Pusat 10110',
        'https://balai-jakarta.go.id/images/office.jpg',
        'https://balai-jakarta.go.id',
        '07.00',
        '16.00',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '550e8400-e29b-41d4-a716-446655440002',
        'Balai Permukiman Bekasi',
        'Bekasi',
        'Kantor cabang untuk layanan konsultasi di wilayah Bekasi dan sekitarnya',
        'info@balai-bekasi.go.id',
        '+62-21-8888888',
        'Jl. Ahmad Yani No.123, Bekasi 17111',
        'https://balai-bekasi.go.id/images/office.jpg',
        'https://balai-bekasi.go.id',
        '07.30',
        '16.30',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- Bandung Region
    (
        '550e8400-e29b-41d4-a716-446655440003',
        'Balai Permukiman Jawa Barat',
        'Bandung',
        'Kantor regional untuk layanan konsultasi permukiman se-Jawa Barat',
        'info@balai-jabar.go.id',
        '+62-22-7777777',
        'Jl. Soekarno Hatta No.456, Bandung 40161',
        'https://balai-jabar.go.id/images/office.jpg',
        'https://balai-jabar.go.id',
        '07.00',
        '16.00',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '550e8400-e29b-41d4-a716-446655440004',
        'Balai Permukiman Cirebon',
        'Cirebon',
        'Kantor cabang untuk wilayah Cirebon dan sekitarnya',
        'info@balai-cirebon.go.id',
        '+62-231-206666',
        'Jl. Siliwangi No.789, Cirebon 45121',
        'https://balai-cirebon.go.id/images/office.jpg',
        'https://balai-cirebon.go.id',
        '08.00',
        '16.30',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- Yogyakarta Region
    (
        '550e8400-e29b-41d4-a716-446655440005',
        'Balai Permukiman Yogyakarta',
        'Yogyakarta',
        'Kantor regional untuk layanan konsultasi permukiman di Yogyakarta dan sekitarnya',
        'info@balai-yogya.go.id',
        '+62-274-555555',
        'Jl. Malioboro No.234, Yogyakarta 55271',
        'https://balai-yogya.go.id/images/office.jpg',
        'https://balai-yogya.go.id',
        '07.30',
        '16.00',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- Semarang Region
    (
        '550e8400-e29b-41d4-a716-446655440006',
        'Balai Permukiman Jawa Tengah',
        'Semarang',
        'Kantor regional untuk layanan konsultasi se-Jawa Tengah',
        'info@balai-jateng.go.id',
        '+62-24-444444',
        'Jl. Pemuda No.567, Semarang 50141',
        'https://balai-jateng.go.id/images/office.jpg',
        'https://balai-jateng.go.id',
        '07.00',
        '16.00',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- Surabaya Region
    (
        '550e8400-e29b-41d4-a716-446655440007',
        'Balai Permukiman Jawa Timur',
        'Surabaya',
        'Kantor regional untuk layanan konsultasi permukiman se-Jawa Timur',
        'info@balai-jatim.go.id',
        '+62-31-333333',
        'Jl. Adi Sumarmo No.890, Surabaya 60188',
        'https://balai-jatim.go.id/images/office.jpg',
        'https://balai-jatim.go.id',
        '07.00',
        '16.30',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '550e8400-e29b-41d4-a716-446655440008',
        'Balai Permukiman Gresik',
        'Gresik',
        'Kantor cabang untuk wilayah Gresik dan Sidoarjo',
        'info@balai-gresik.go.id',
        '+62-31-222222',
        'Jl. Raya Gresik No.345, Gresik 61111',
        'https://balai-gresik.go.id/images/office.jpg',
        'https://balai-gresik.go.id',
        '08.00',
        '16.30',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- Medan Region
    (
        '550e8400-e29b-41d4-a716-446655440009',
        'Balai Permukiman Sumatera Utara',
        'Medan',
        'Kantor regional untuk layanan konsultasi di Sumatera Utara',
        'info@balai-sumut.go.id',
        '+62-61-111111',
        'Jl. Gatot Subroto No.234, Medan 20123',
        'https://balai-sumut.go.id/images/office.jpg',
        'https://balai-sumut.go.id',
        '07.30',
        '16.00',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- Makassar Region
    (
        '550e8400-e29b-41d4-a716-446655440010',
        'Balai Permukiman Sulawesi Selatan',
        'Makassar',
        'Kantor regional untuk layanan konsultasi di Sulawesi Selatan',
        'info@balai-sulsel.go.id',
        '+62-411-999999',
        'Jl. Ahmad Yani No.567, Makassar 90231',
        'https://balai-sulsel.go.id/images/office.jpg',
        'https://balai-sulsel.go.id',
        '08.00',
        '16.30',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
