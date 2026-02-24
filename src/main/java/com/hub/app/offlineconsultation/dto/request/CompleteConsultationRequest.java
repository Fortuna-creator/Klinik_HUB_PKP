package com.hub.app.offlineconsultation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PUT /api/consultations/{consultationId}/complete
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteConsultationRequest {

    private String idempotencyKey;     // must match Idempotency-Key header

    @NotBlank(message = "verificationId is required")
    private String verificationId;     // the verification code given to the user
}
