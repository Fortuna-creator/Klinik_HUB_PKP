package com.hub.app.consultation.model;

public enum SurveyStatus {
    PENDING,      // Menunggu konfirmasi jadwal
    APPROVED,     // Jadwal disetujui
    REJECTED,     // Jadwal ditolak
    RESCHEDULED,  // Diajukan jadwal baru
    COMPLETED     // Survey sudah selesai
}