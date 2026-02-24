package com.hub.app.offlineconsultation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PUT /api/consultations/{consultationId}/confirm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmConsultationRequest {

    private String idempotencyKey;   // must match Idempotency-Key header

    private String notes;            // optional
}
