package com.hub.app.offlineconsultation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response item for GET /api/confirmations/inbox
 * Represents a confirmed consultation booking with session key.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingConfirmationResponse {

    private String id;                      // Consultation ID (UUID)
    private String userId;                  // Warga user ID (as string)
    private String balaiLocation;           // e.g., "Jakarta Pusat"
    private String balaiId;                 // UUID of the Balai
    private String date;                    // ISO date: "2026-03-15"
    private String timeSlot;                // e.g., "09.00 - 10.00"
    private String sessionKey;              // Secret key for ending consultation (ABC123XYZ format)
    private String status;                  // Consultation status
    private Instant confirmationDate;       // When Balai confirmed
    private Instant createdAt;
    private Instant updatedAt;
}
