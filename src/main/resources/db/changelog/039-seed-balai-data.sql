--liquibase formatted sql

--changeset hub:039-seed-balai-data context:dev,test splitStatements:false

INSERT INTO balai (id, name, location, description, email, phone_number, address, image_url, web_url, working_hours_start, working_hours_end, created_at, updated_at)
VALUES
    -- SUMATERA REGION (5 offices)
    ('550e8400-e29b-41d4-a716-446655440001', 'Balai P3KP Sumatera I', 'Kota Banda Aceh', 'Kantor regional Sumatera I melayani Provinsi Nanggroe Aceh Darussalam', 'bp2p.sumatera1@pu.go.id', '(0651) 23230', 'Jl. Jend Sudirman No. 32 Lamtemen Timur, Kota Banda Aceh', '', '', '07.30', '16.00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440002', 'Balai P3KP Sumatera II', 'Kota Medan', 'Kantor regional Sumatera II melayani Provinsi Sumatera Utara', 'bp2p.sumatera2@pu.go.id', '(061) 80032544', 'Jl. Suluh No.99 A, Kel. Sidorejo Hilir, Kec. Medan Tembung, Kota Medan', '', '', '07.30', '16.00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440003', 'Balai P3KP Sumatera III', 'Kota Pekanbaru', 'Kantor regional Sumatera III melayani Provinsi Sumatera Barat, Riau, dan Kepulauan Riau', 'bp2p.sumatera3@pu.go.id', '(0761) 21234', 'Jl. Diponegoro No. 45, Kel. Cinta Raja, Kec. Sail, Kota Pekanbaru', '', '', '07.30', '16.00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440004', 'Balai P3KP Sumatera IV', 'Kota Jambi', 'Kantor regional Sumatera IV melayani Provinsi Jambi dan Bengkulu', 'bp2p.sumatera4@pu.go.id', '(0741) 62419', 'Jl. Letjend Suprapto No.28.A, Telanaipura, Kota Jambi', '', '', '07.30', '16.00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440005', 'Balai P3KP Sumatera V', 'Kota Palembang', 'Kantor regional Sumatera V melayani Provinsi Sumatera Selatan, Bangka Belitung, dan Lampung', 'bp2p.sumatera5@pu.go.id', '089616937444', 'Jl. Jenderal Sudirman/ Inspektur Yazid No.7490, Km 2,5 Kota Palembang', '', '', '07.30', '16.00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- JAWA REGION (4 offices)
    ('550e8400-e29b-41d4-a716-446655440006', 'Balai P3KP Jawa I', 'Jakarta Selatan', 'Kantor regional Jawa I melayani Provinsi DK Jakarta dan Banten', 'bp2p.jawa1@pu.go.id', '(021) 72796158', 'Gedung Wisma Karya, Jl. Wijaya I No.59, Petogogan, Kebayoran Baru, Jakarta Selatan', '', '', '07.00', '16.00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440007', 'Balai P3KP Jawa II', 'Kota Bandung', 'Kantor regional Jawa II melayani Provinsi Jawa Barat', 'bp2p.jawa2@pu.go.id', '(022) 4203643', 'Jl. Lengkong Besar No. 10, Cikawao, Kec. Lengkong, Kota Bandung', '', '', '07.00', '16.00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440008', 'Balai P3KP Jawa III', 'Kab. Sleman', 'Kantor regional Jawa III melayani Provinsi Jawa Tengah dan D.I. Yogyakarta', 'bp2p.jawa3@pu.go.id', '(0274) 514330', 'Jl. Laksda Adisucipto No. 165, Catur Tunggal, Kec. Depok, Kab. Sleman', '', '', '07.30', '16.00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440009', 'Balai P3KP Jawa IV', 'Kota Surabaya', 'Kantor regional Jawa IV melayani Provinsi Jawa Timur', 'bp2p.jawa4@pu.go.id', '(031) 8292143', 'Jl. Gayung Kebonsari No. 50, Ketintang, Kota Surabaya', '', '', '07.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- KALIMANTAN REGION (2 offices)
    ('550e8400-e29b-41d4-a716-446655440010', 'Balai P3KP Kalimantan I', 'Kota Pontianak', 'Kantor regional Kalimantan I melayani Provinsi Kalimantan Barat dan Kalimantan Tengah', 'bp2p.kalimantan1@pu.go.id', '(0561) 732047', 'Jl. Imam Bonjol No. 116, Benua Melayu Darat, Kec. Pontianak Selatan, Kota Pontianak', '', '', '08.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440011', 'Balai P3KP Kalimantan II', 'Kota Samarinda', 'Kantor regional Kalimantan II melayani Provinsi Kalimantan Selatan, Kalimantan Timur, dan Kalimantan Utara', 'bp2pkalimantan2@gmail.com', '(0541) 741635', 'Jl. Cendana No. 167 Kompleks PDAM, Kota Samarinda', '', '', '08.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SULAWESI REGION (3 offices)
    ('550e8400-e29b-41d4-a716-446655440012', 'Balai P3KP Sulawesi I', 'Kota Manado', 'Kantor regional Sulawesi I melayani Provinsi Sulawesi Utara, Gorontalo, dan Maluku Utara', 'balaipnpsul1@gmail.com', '(0431) 852234', 'Jl. Ringroad I, Ruko Business 8 Blok B No. 69, Kel. Taas, Kec. Tikala, Kota Manado', '', '', '08.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440013', 'Balai P3KP Sulawesi II', 'Kota Palu', 'Kantor regional Sulawesi II melayani Provinsi Sulawesi Tengah dan Sulawesi Barat', 'bp2p.sulawesill@gmail.com', '(0451) 422033', 'Jl. Sungai Surumana No. 35, Siranindi, Kec. Palu Barat, Kota Palu', '', '', '08.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440014', 'Balai P3KP Sulawesi III', 'Kota Makassar', 'Kantor regional Sulawesi III melayani Provinsi Sulawesi Selatan dan Sulawesi Tenggara', 'bp2p.sulawesi3@pu.go.id', '(0411) 442234', 'Jl. Penjernihan Raya No. 22, Komp. PDAM Panakkukang, Kota Makassar', '', '', '08.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- NUSA TENGGARA REGION (2 offices)
    ('550e8400-e29b-41d4-a716-446655440015', 'Balai P3KP Nusa Tenggara I', 'Kab. Lombok Barat', 'Kantor regional Nusa Tenggara I melayani Provinsi Nusa Tenggara Barat dan Bali', 'bp2p.nusatenggara1@pu.go.id', '(0370) 633120', 'Jl. TGH. Faesal No. 555, Bengkel, Kec. Labuapi, Kab. Lombok Barat', '', '', '08.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440016', 'Balai P3KP Nusa Tenggara II', 'Kota Kupang', 'Kantor regional Nusa Tenggara II melayani Provinsi Nusa Tenggara Timur', 'bp2p.nusatenggara2@pu.go.id', '(0380) 821360', 'Jl. W.J. Lalamentik No. 20, Kec. Oebobo, Kota Kupang', '', '', '08.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- MALUKU REGION (1 office)
    ('550e8400-e29b-41d4-a716-446655440017', 'Balai P3KP Maluku', 'Kota Ambon', 'Kantor regional Maluku melayani Provinsi Maluku', 'bp3kp.maluku@pkp.go.id', '(0911) 352125', 'Jl. Mr. Chr. Soplanit, Rumah Tiga, Kec. Teluk Ambon, Kota Ambon', '', '', '08.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- PAPUA REGION (2 offices)
    ('550e8400-e29b-41d4-a716-446655440018', 'Balai P3KP Papua I', 'Kota Jayapura', 'Kantor regional Papua I melayani Provinsi Papua, Papua Tengah, Papua Pegunungan, dan Papua Selatan', 'bp2p.papua1@pu.go.id', '(0967) 581125', 'Kompleks Ruko, Jl. Kelapa Dua Entrop, Kec. Jayapura Selatan, Kota Jayapura', '', '', '08.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440019', 'Balai P3KP Papua II', 'Kab. Manokwari', 'Kantor regional Papua II melayani Provinsi Papua Barat dan Papua Barat Daya', 'bp2p.papua2@pu.go.id', '(0986) 211234', 'Jl. Merdeka Kel. Padarni, Kec. Manokwari Barat, Kab. Manokwari', '', '', '08.00', '16.30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
