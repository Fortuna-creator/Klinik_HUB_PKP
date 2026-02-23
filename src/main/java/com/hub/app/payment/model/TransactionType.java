package com.hub.app.payment.model;

public enum TransactionType {
    TOP_UP,           // Manual/admin topup
    GATEWAY_TOPUP,    // Payment gateway topup
    ESCROW_HOLD,      // Transfer to escrow
    ESCROW_RELEASE,   // Release from escrow to consultant
    ESCROW_REFUND,    // Refund from escrow to home owner
    TRANSFER,         // Direct transfer between users
    PAYMENT,          // General payment transaction
    WITHDRAWAL        // Future withdrawal feature
}