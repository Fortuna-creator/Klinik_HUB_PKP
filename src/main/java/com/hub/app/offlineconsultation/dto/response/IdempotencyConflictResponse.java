package com.hub.app.offlineconsultation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned as 409 Conflict when an idempotency key collision is detected.
 * Matches the contract's error shapes for REQUEST_DUPLICATE / DUPLICATE_REQUEST.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdempotencyConflictResponse {

    private String error;
    private String message;
    private int statusCode;
    private String idempotencyKey;
    private String status;           // "PROCESSING" | "SUCCESS"
}
