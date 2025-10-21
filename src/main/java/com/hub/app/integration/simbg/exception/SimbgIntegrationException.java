package com.hub.app.integration.simbg.exception;

/**
 * Custom exception for SIMBG integration errors.
 */
public class SimbgIntegrationException extends RuntimeException {

    private final int statusCode;

    public SimbgIntegrationException(String message) {
        super(message);
        this.statusCode = 500; // Default to internal server error
    }

    public SimbgIntegrationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public SimbgIntegrationException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }

    public SimbgIntegrationException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}