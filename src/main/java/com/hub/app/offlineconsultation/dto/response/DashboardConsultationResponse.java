package com.hub.app.offlineconsultation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consultation detail for Balai dashboard — excludes consultationId (session key)
 * to prevent Balai staff from seeing Warga verification codes.
 * 
 * Used by: GET /api/dashboard/consultations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardConsultationResponse {

    private String id;
    private UserSummaryDto user;
    private String balaiLocation;
    private String balaiId;
    private String date;             // YYYY-MM-DD
    private String timeSlot;
    private String status;
    private String topic;
    private String description;
    private String cancellationReason;
    private String notes;
    private String createdAt;        // ISO 8601 timestamp
    private String updatedAt;        // ISO 8601 timestamp
}
