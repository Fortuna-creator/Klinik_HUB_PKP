package com.hub.app.offlineconsultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for POST /api/consultations/{id}/verify.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyConsultationResponse {

    private boolean verified;
    private String message;
    private ConsultationDetailResponse consultation;
}
