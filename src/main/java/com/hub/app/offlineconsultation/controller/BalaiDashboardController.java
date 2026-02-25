package com.hub.app.offlineconsultation.controller;

import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.offlineconsultation.dto.request.CompleteConsultationRequest;
import com.hub.app.offlineconsultation.dto.request.ConfirmConsultationRequest;
import com.hub.app.offlineconsultation.dto.request.RejectConsultationRequest;
import com.hub.app.offlineconsultation.dto.response.ConsultationActionResponse;
import com.hub.app.offlineconsultation.dto.response.ConsultationDetailResponse;
import com.hub.app.offlineconsultation.dto.response.DashboardStatsResponse;
import com.hub.app.offlineconsultation.service.IdempotencyService;
import com.hub.app.offlineconsultation.service.OfflineConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for Balai (admin) dashboard operations.
 *
 * <p>Endpoints handled:</p>
 * <ul>
 *   <li>{@code GET  /api/dashboard/consultations}        — List consultations for a Balai</li>
 *   <li>{@code PUT  /api/consultations/{id}/confirm}     — Confirm a consultation</li>
 *   <li>{@code PUT  /api/consultations/{id}/reject}      — Reject a consultation</li>
 *   <li>{@code PUT  /api/consultations/{id}/complete}    — Complete a consultation</li>
 *   <li>{@code GET  /api/dashboard/stats}                — Dashboard statistics</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Balai Dashboard", description = "Balai admin dashboard endpoints")
public class BalaiDashboardController {

    private final OfflineConsultationService consultationService;
    private final IdempotencyService idempotencyService;

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/dashboard/consultations
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Get consultations for the Balai dashboard, with optional date and status filters.
     */
    @GetMapping("/api/dashboard/consultations")
    @Operation(summary = "Get dashboard consultations")
    public ResponseEntity<List<ConsultationDetailResponse>> getDashboardConsultations(
            @RequestParam String balaiId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails principal) {

        List<ConsultationDetailResponse> result =
                consultationService.getDashboardConsultations(balaiId, date, status, page, size);
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUT /api/consultations/{consultationId}/confirm
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Confirm a pending consultation. Requires Idempotency-Key header.
     * The request body may optionally include an idempotencyKey field that
     * must match the header value.
     */
    @PutMapping("/api/consultations/{consultationId}/confirm")
    @Operation(summary = "Confirm a consultation")
    public ResponseEntity<Object> confirmConsultation(
            @PathVariable String consultationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ConfirmConsultationRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        validateIdempotencyKey(idempotencyKey, request.getIdempotencyKey());

        Long userId = resolveUserId(principal.getUsername());
        Optional<ResponseEntity<Object>> cached =
                idempotencyService.checkAndCreate(idempotencyKey, userId, "PUT /api/consultations/confirm");
        if (cached.isPresent()) {
            return cached.get();
        }

        try {
            UUID uuid = parseUuid(consultationId);
            ConsultationActionResponse response = consultationService.confirmConsultation(uuid, request);
            idempotencyService.markSuccess(idempotencyKey, response, HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            idempotencyService.markFailed(idempotencyKey);
            throw ex;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUT /api/consultations/{consultationId}/reject
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Reject/cancel a consultation. Requires Idempotency-Key header.
     */
    @PutMapping("/api/consultations/{consultationId}/reject")
    @Operation(summary = "Reject a consultation")
    public ResponseEntity<Object> rejectConsultation(
            @PathVariable String consultationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody RejectConsultationRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        validateIdempotencyKey(idempotencyKey, request.getIdempotencyKey());

        Long userId = resolveUserId(principal.getUsername());
        Optional<ResponseEntity<Object>> cached =
                idempotencyService.checkAndCreate(idempotencyKey, userId, "PUT /api/consultations/reject");
        if (cached.isPresent()) {
            return cached.get();
        }

        try {
            UUID uuid = parseUuid(consultationId);
            ConsultationActionResponse response = consultationService.rejectConsultation(uuid, request);
            idempotencyService.markSuccess(idempotencyKey, response, HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            idempotencyService.markFailed(idempotencyKey);
            throw ex;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUT /api/consultations/{consultationId}/complete
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Complete a consultation and assign verification code. Requires Idempotency-Key header.
     */
    @PutMapping("/api/consultations/{consultationId}/complete")
    @Operation(summary = "Complete a consultation")
    public ResponseEntity<Object> completeConsultation(
            @PathVariable String consultationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CompleteConsultationRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        validateIdempotencyKey(idempotencyKey, request.getIdempotencyKey());

        Long userId = resolveUserId(principal.getUsername());
        Optional<ResponseEntity<Object>> cached =
                idempotencyService.checkAndCreate(idempotencyKey, userId, "PUT /api/consultations/complete");
        if (cached.isPresent()) {
            return cached.get();
        }

        try {
            UUID uuid = parseUuid(consultationId);
            ConsultationActionResponse response = consultationService.completeConsultation(uuid, request);
            idempotencyService.markSuccess(idempotencyKey, response, HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            idempotencyService.markFailed(idempotencyKey);
            throw ex;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/dashboard/stats
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Get today's consultation statistics for a specific Balai.
     */
    @GetMapping("/api/dashboard/stats")
    @Operation(summary = "Get dashboard stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @RequestParam String balaiId,
            @AuthenticationPrincipal UserDetails principal) {

        DashboardStatsResponse stats = consultationService.getDashboardStats(balaiId);
        return ResponseEntity.ok(stats);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Validates that the Idempotency-Key header is present and matches the body field
     * (if the body also includes an idempotencyKey).
     */
    private void validateIdempotencyKey(String headerKey, String bodyKey) {
        if (headerKey == null || headerKey.isBlank()) {
            throw new DomainException(ErrorCode.OC_IDEMPOTENCY_KEY_MISSING,
                    "Idempotency-Key header is required");
        }
        if (bodyKey != null && !bodyKey.isBlank() && !headerKey.equals(bodyKey)) {
            throw new DomainException(ErrorCode.OC_IDEMPOTENCY_KEY_MISMATCH,
                    "Idempotency-Key header does not match request body");
        }
    }

    /**
     * Resolves the user's Long ID from their email for idempotency tracking.
     */
    private Long resolveUserId(String email) {
        return consultationService.resolveUserId(email);
    }

    /**
     * Parses a path variable as UUID.
     */
    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DomainException(ErrorCode.BAD_REQUEST,
                    "Invalid consultation ID format: " + value);
        }
    }
}
