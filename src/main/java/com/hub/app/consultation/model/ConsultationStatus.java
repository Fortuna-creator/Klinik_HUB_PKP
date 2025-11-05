package com.hub.app.consultation.model;

public enum ConsultationStatus {
    MENUNGGU_KONFIRMASI_KONSULTAN, // Home Owner mengajukan, menunggu konsultan terima
    MENYIAPKAN_KONTRAK,            // Konsultan sudah terima, sedang menyiapkan kontrak
    MENUNGGU_APPROVAL_KONTRAK,     // Kontrak diajukan untuk approval homeowner
    KONTRAK_DISETUJUI,             // Kontrak disetujui, menunggu tanda tangan
    MENUNGGU_TANDA_TANGAN_KONTRAK, // Kontrak disetujui, salah satu pihak sudah sign, menunggu pihak lainnya
    MENUNGGU_REQUEST_PAYMENT,      // Kontrak sudah ditandatangani, menunggu konsultan request payment
    MENUNGGU_PEMBAYARAN,           // Payment sudah direquest, menunggu homeowner bayar
    MENUNGGU_JADWAL_SURVEY,        // Kontrak sudah ditandatangani, mengajukan jadwal survey
    MENUNGGU_APPROVAL_JADWAL,      // Home Owner dapat tolak atau propose jadwal baru
    SURVEY_DIJADWALKAN,            // Kedua pihak sudah setuju jadwal
    SURVEY_SELESAI,                // Survey sudah dilakukan
    AKTIF,                         // Konsultasi aktif (chat)
    REVISI,                        // Dalam proses revisi
    FINAL,                         // Konsultasi selesai
    SELESAI                        // Semua proses selesai
}