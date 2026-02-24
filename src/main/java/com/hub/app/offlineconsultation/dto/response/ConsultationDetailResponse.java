package com.hub.app.offlineconsultation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full consultation detail — used both as the nested {@code "consultation"} object
 * in booking/confirm/reject/complete responses, and as list items in
 * GET /api/consultations/me and GET /api/dashboard/consultations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsultationDetailResponse {

    private String id;
    private UserSummaryDto user;
    private String balaiLocation;
    private String balaiId;
    private String date;             // YYYY-MM-DD
    private String timeSlot;
    private String status;
    private String consultationId;   // verification code (optional, set on complete)
    private String topic;
    private String description;
    private String cancellationReason;
    private String notes;
    private String createdAt;        // ISO 8601 timestamp
    private String updatedAt;        // ISO 8601 timestamp
}
