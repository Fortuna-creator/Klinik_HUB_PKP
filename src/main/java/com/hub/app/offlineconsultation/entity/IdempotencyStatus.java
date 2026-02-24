package com.hub.app.offlineconsultation.entity;

/**
 * Lifecycle status of an idempotency record.
 */
public enum IdempotencyStatus {
    PROCESSING,
    SUCCESS,
    FAILED
}
