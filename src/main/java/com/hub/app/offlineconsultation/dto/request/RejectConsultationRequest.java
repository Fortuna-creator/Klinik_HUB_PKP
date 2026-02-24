package com.hub.app.offlineconsultation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PUT /api/consultations/{consultationId}/reject
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectConsultationRequest {

    private String idempotencyKey;   // must match Idempotency-Key header

    @NotBlank(message = "reason is required")
    private String reason;
}
