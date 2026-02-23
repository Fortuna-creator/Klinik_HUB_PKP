package com.hub.app.escrow.model;

public enum EscrowStatus {
    PENDING,      // Escrow account created, waiting for funds
    ACTIVE,       // Funds held in escrow
    RELEASED,     // Funds released to consultant
    REFUNDED,     // Funds refunded to home owner
    CANCELLED     // Escrow cancelled
}