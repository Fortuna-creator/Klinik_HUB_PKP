--liquibase formatted sql

--changeset hub:040-seed-test-users context:dev,test splitStatements:false

INSERT INTO users (
    id, email, name, password, phone, address, role, status, activity_status, 
    created_at, updated_at
)
VALUES
    -- HOMEOWNER TEST USERS (5)
    (5001, 'homeowner1@test.com', 'Budi Santoso', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '081234567890', 'Jl. Merdeka No. 123, Jakarta Pusat', 'HOMEOWNER', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5002, 'homeowner2@test.com', 'Siti Nurhaliza', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '081987654321', 'Jl. Sudirman No. 456, Bandung', 'HOMEOWNER', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5003, 'homeowner3@test.com', 'Ahmad Wijaya', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '081456789012', 'Jl. Gatot Subroto No. 789, Surabaya', 'HOMEOWNER', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5004, 'homeowner4@test.com', 'Dewi Lestari', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '082111222333', 'Jl. Malioboro No. 321, Yogyakarta', 'HOMEOWNER', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5005, 'homeowner5@test.com', 'Rany Hermawan', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '082333444555', 'Jl. Pemuda No. 654, Semarang', 'HOMEOWNER', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- BALAI TEST USERS (19) - Matching 19 regional offices

    -- SUMATERA REGION (5)
    (6001, 'bp2p.sumatera1@pu.go.id', 'Balai P3KP Sumatera I', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0651) 23230', 'Jl. Jend Sudirman No. 32 Lamtemen Timur, Kota Banda Aceh', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6002, 'bp2p.sumatera2@pu.go.id', 'Balai P3KP Sumatera II', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(061) 80032544', 'Jl. Suluh No.99 A, Kel. Sidorejo Hilir, Kec. Medan Tembung, Kota Medan', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6003, 'bp2p.sumatera3@pu.go.id', 'Balai P3KP Sumatera III', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0761) 21234', 'Jl. Diponegoro No. 45, Kel. Cinta Raja, Kec. Sail, Kota Pekanbaru', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6004, 'bp2p.sumatera4@pu.go.id', 'Balai P3KP Sumatera IV', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0741) 62419', 'Jl. Letjend Suprapto No.28.A, Telanaipura, Kota Jambi', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6005, 'bp2p.sumatera5@pu.go.id', 'Balai P3KP Sumatera V', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '089616937444', 'Jl. Jenderal Sudirman/ Inspektur Yazid No.7490, Km 2,5 Kota Palembang', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- JAWA REGION (4)
    (6006, 'bp2p.jawa1@pu.go.id', 'Balai P3KP Jawa I', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(021) 72796158', 'Gedung Wisma Karya, Jl. Wijaya I No.59, Petogogan, Kebayoran Baru, Jakarta Selatan', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6007, 'bp2p.jawa2@pu.go.id', 'Balai P3KP Jawa II', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(022) 4203643', 'Jl. Lengkong Besar No. 10, Cikawao, Kec. Lengkong, Kota Bandung', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6008, 'bp2p.jawa3@pu.go.id', 'Balai P3KP Jawa III', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0274) 514330', 'Jl. Laksda Adisucipto No. 165, Catur Tunggal, Kec. Depok, Kab. Sleman', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6009, 'bp2p.jawa4@pu.go.id', 'Balai P3KP Jawa IV', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(031) 8292143', 'Jl. Gayung Kebonsari No. 50, Ketintang, Kota Surabaya', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- KALIMANTAN REGION (2)
    (6010, 'bp2p.kalimantan1@pu.go.id', 'Balai P3KP Kalimantan I', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0561) 732047', 'Jl. Imam Bonjol No. 116, Benua Melayu Darat, Kec. Pontianak Selatan, Kota Pontianak', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6011, 'bp2pkalimantan2@gmail.com', 'Balai P3KP Kalimantan II', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0541) 741635', 'Jl. Cendana No. 167 Kompleks PDAM, Kota Samarinda', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- SULAWESI REGION (3)
    (6012, 'balaipnpsul1@gmail.com', 'Balai P3KP Sulawesi I', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0431) 852234', 'Jl. Ringroad I, Ruko Business 8 Blok B No. 69, Kel. Taas, Kec. Tikala, Kota Manado', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6013, 'bp2p.sulawesill@gmail.com', 'Balai P3KP Sulawesi II', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0451) 422033', 'Jl. Sungai Surumana No. 35, Siranindi, Kec. Palu Barat, Kota Palu', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6014, 'bp2p.sulawesi3@pu.go.id', 'Balai P3KP Sulawesi III', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0411) 442234', 'Jl. Penjernihan Raya No. 22, Komp. PDAM Panakkukang, Kota Makassar', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- NUSA TENGGARA REGION (2)
    (6015, 'bp2p.nusatenggara1@pu.go.id', 'Balai P3KP Nusa Tenggara I', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0370) 633120', 'Jl. TGH. Faesal No. 555, Bengkel, Kec. Labuapi, Kab. Lombok Barat', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6016, 'bp2p.nusatenggara2@pu.go.id', 'Balai P3KP Nusa Tenggara II', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0380) 821360', 'Jl. W.J. Lalamentik No. 20, Kec. Oebobo, Kota Kupang', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- MALUKU REGION (1)
    (6017, 'bp3kp.maluku@pkp.go.id', 'Balai P3KP Maluku', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0911) 352125', 'Jl. Mr. Chr. Soplanit, Rumah Tiga, Kec. Teluk Ambon, Kota Ambon', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- PAPUA REGION (2)
    (6018, 'bp2p.papua1@pu.go.id', 'Balai P3KP Papua I', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0967) 581125', 'Kompleks Ruko, Jl. Kelapa Dua Entrop, Kec. Jayapura Selatan, Kota Jayapura', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6019, 'bp2p.papua2@pu.go.id', 'Balai P3KP Papua II', '$2a$10$d.vrhv2B4oZtDdiyWkW5duaeBuKC7FiyI.wjZ0aJV8JddLQeewOUW', '(0986) 211234', 'Jl. Merdeka Kel. Padarni, Kec. Manokwari Barat, Kab. Manokwari', 'BALAI', 'VERIFIED', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
