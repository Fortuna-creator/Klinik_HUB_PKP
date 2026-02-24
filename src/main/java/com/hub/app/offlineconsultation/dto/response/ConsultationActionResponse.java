package com.hub.app.offlineconsultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared response shape for PUT confirm / reject / complete endpoints.
 * Contract responses all follow: { id, status, message, consultation }.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationActionResponse {

    private String id;
    private String status;
    private String message;
    private ConsultationDetailResponse consultation;
}
