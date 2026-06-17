package com.hub.app.offlineconsultation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PUT /api/consultations/{consultationId}/complete
 * 
 * Marks a consultation as complete. The Balai must provide the session key
 * that was generated at confirmation time. This ensures the Balai has the
 * correct code before closing the consultation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteConsultationRequest {

    private String idempotencyKey;     // must match Idempotency-Key header
    private String sessionKey;         // 8-digit code generated at confirmation, validated before completion
}

