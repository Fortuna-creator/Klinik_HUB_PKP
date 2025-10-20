package com.hub.app.consultation.model;

public enum ConsultationStatus {
    MENUNGGU_KONFIRMASI_KONSULTAN, // Home Owner mengajukan, menunggu konsultan terima
    MENUNGGU_JADWAL_SURVEY,       // Konsultan sudah terima, mengajukan jadwal survey
    MENUNGGU_APPROVAL_JADWAL,     // Home Owner dapat tolak atau propose jadwal baru
    SURVEY_DIJADWALKAN,           // Kedua pihak sudah setuju jadwal
    SURVEY_SELESAI,               // Survey sudah dilakukan
    AKTIF,                        // Konsultasi aktif (chat)
    REVISI,                       // Dalam proses revisi
    FINAL,                        // Konsultasi selesai
    SELESAI                       // Semua proses selesai
}