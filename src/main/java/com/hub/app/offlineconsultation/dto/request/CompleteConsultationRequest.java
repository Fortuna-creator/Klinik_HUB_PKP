package com.hub.app.offlineconsultation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PUT /api/consultations/{consultationId}/complete
 * 
 * Marks a consultation as complete. The system automatically generates
 * a random session key that is given to the Warga to verify/end the session.
 * Balai cannot provide or see this code (security feature).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteConsultationRequest {

    private String idempotencyKey;     // must match Idempotency-Key header
    
    // No verificationId field - system generates random code automatically
}

