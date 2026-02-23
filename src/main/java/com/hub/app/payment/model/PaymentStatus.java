package com.hub.app.payment.model;

public enum PaymentStatus {
    MENUNGGU,
    ESCROW_PENDING,    // Menunggu konfirmasi escrow
    ESCROW_HOLD,       // Dana sudah di-hold di escrow
    ESCROW_RELEASED,   // Dana sudah dilepas ke konsultan
    ESCROW_REFUNDED,   // Dana dikembalikan ke home owner
    BERHASIL,
    GAGAL
}