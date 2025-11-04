package com.hub.app.consultation.model;

public enum ContractStatus {
    DRAFT,              // Contract sedang dibuat konsultan
    REQUEST_FOR_APPROVAL, // Diajukan untuk approval homeowner
    REVISION_REQUESTED, // Homeowner meminta revisi
    APPROVED,           // Disetujui homeowner
    REJECTED,           // Ditolak homeowner
    HOMEOWNER_SIGNED,   // Homeowner sudah tanda tangan, menunggu konsultan
    CONSULTANT_SIGNED,  // Konsultan sudah tanda tangan, menunggu homeowner
    SIGNED              // Kedua pihak sudah ditandatangani
}