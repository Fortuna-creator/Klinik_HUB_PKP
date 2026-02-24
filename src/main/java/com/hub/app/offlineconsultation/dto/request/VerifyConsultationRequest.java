package com.hub.app.offlineconsultation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POST /api/consultations/{consultationId}/verify
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyConsultationRequest {

    @NotBlank(message = "verificationCode is required")
    private String verificationCode;
}
