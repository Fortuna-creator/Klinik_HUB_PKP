package com.hub.app.error;

import java.util.UUID;

public class ConsultationWorkflowException extends DomainException {

    public ConsultationWorkflowException(String message) {
        super(ErrorCode.WORKFLOW_ERROR, message);
    }

    public ConsultationWorkflowException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static ConsultationWorkflowException invalidStatusTransition(String currentStatus, String targetStatus) {
        return new ConsultationWorkflowException(
            "Invalid status transition from " + currentStatus + " to " + targetStatus
        );
    }

    public static ConsultationWorkflowException consultationNotFound(UUID consultationId) {
        return new ConsultationWorkflowException(
            ErrorCode.CONSULTATION_NOT_FOUND, "Consultation not found: " + consultationId
        );
    }

    public static ConsultationWorkflowException accessDenied(String action) {
        return new ConsultationWorkflowException(
            ErrorCode.UNAUTHORIZED, "Access denied for action: " + action
        );
    }

    public static ConsultationWorkflowException documentNotFound(String documentType) {
        return new ConsultationWorkflowException(
            "Document not found: " + documentType
        );
    }

    public static ConsultationWorkflowException contractNotFound() {
        return new ConsultationWorkflowException(
            "Contract not found for this consultation"
        );
    }
}