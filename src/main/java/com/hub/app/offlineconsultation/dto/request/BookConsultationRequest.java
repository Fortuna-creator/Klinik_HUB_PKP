package com.hub.app.offlineconsultation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POST /api/consultations/book
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookConsultationRequest {

    @NotBlank(message = "balaiId is required")
    private String balaiId;

    @NotBlank(message = "date is required")
    private String date;           // ISO 8601: YYYY-MM-DD

    @NotBlank(message = "timeSlot is required")
    private String timeSlot;       // "HH.MM - HH.MM"

    @NotBlank(message = "topic is required")
    private String topic;

    private String description;    // optional
}
