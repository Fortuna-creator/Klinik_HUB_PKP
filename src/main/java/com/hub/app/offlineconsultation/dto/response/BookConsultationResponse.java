package com.hub.app.offlineconsultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for POST /api/consultations/book (201 Created).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookConsultationResponse {

    private String id;
    private String status;       // "menunggu_konfirmasi"
    private String message;
    private ConsultationDetailResponse consultation;
}
