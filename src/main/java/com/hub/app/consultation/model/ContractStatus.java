package com.hub.app.consultation.model;

public enum ContractStatus {
    DRAFT,              // Contract sedang dibuat konsultan
    REQUEST_FOR_APPROVAL, // Diajukan untuk approval homeowner
    APPROVED,           // Disetujui homeowner
    REJECTED,           // Ditolak homeowner
    SIGNED              // Sudah ditandatangani
}