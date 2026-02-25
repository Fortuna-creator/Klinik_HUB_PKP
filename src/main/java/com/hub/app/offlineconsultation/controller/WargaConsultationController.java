package com.hub.app.offlineconsultation.controller;

import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.offlineconsultation.dto.request.BookConsultationRequest;
import com.hub.app.offlineconsultation.dto.request.VerifyConsultationRequest;
import com.hub.app.offlineconsultation.dto.response.BookConsultationResponse;
import com.hub.app.offlineconsultation.dto.response.ConsultationDetailResponse;
import com.hub.app.offlineconsultation.dto.response.VerifyConsultationResponse;
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
 * REST controller for Warga (citizen) consultation operations.
 *
 * <p>Endpoints handled:</p>
 * <ul>
 *   <li>{@code POST /api/consultations/book} — Book a new consultation</li>
 *   <li>{@code GET  /api/consultations/me}   — List user's own consultations</li>
 *   <li>{@code POST /api/consultations/{id}/verify} — Verify consultation code</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Warga Consultations", description = "Citizen-facing consultation endpoints")
public class WargaConsultationController {

    private final OfflineConsultationService consultationService;
    private final IdempotencyService idempotencyService;

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/consultations/book
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Book a new offline consultation.
     *
     * <p>Requires the {@code Idempotency-Key} header to prevent duplicate bookings.
     * If a request with the same key was already processed successfully, the cached
     * response is returned. If still processing, a 409 is returned.</p>
     */
    @PostMapping("/book")
    @Operation(summary = "Book a new consultation")
    public ResponseEntity<Object> bookConsultation(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody BookConsultationRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        String userEmail = principal.getUsername();

        // Validate idempotency key presence
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new DomainException(ErrorCode.OC_IDEMPOTENCY_KEY_MISSING,
                    "Idempotency-Key header is required for booking");
        }

        // Look up user ID for the idempotency record
        Long userId = resolveUserId(userEmail);

        // Check idempotency — may return a cached or conflict response
        Optional<ResponseEntity<Object>> cached =
                idempotencyService.checkAndCreate(idempotencyKey, userId, "POST /api/consultations/book");
        if (cached.isPresent()) {
            return cached.get();
        }

        try {
            BookConsultationResponse response = consultationService.bookConsultation(request, userEmail);
            idempotencyService.markSuccess(idempotencyKey, response, HttpStatus.CREATED.value());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            idempotencyService.markFailed(idempotencyKey);
            throw ex;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/consultations/me
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Get the authenticated Warga's own consultations, with optional status filter
     * and pagination.
     */
    @GetMapping("/me")
    @Operation(summary = "Get my consultations")
    public ResponseEntity<List<ConsultationDetailResponse>> getMyConsultations(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails principal) {

        String userEmail = principal.getUsername();
        List<ConsultationDetailResponse> result =
                consultationService.getMyConsultations(userEmail, status, page, size);
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/consultations/{consultationId}/verify
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Verify a consultation using the verification code provided by the Balai.
     */
    @PostMapping("/{consultationId}/verify")
    @Operation(summary = "Verify a consultation")
    public ResponseEntity<VerifyConsultationResponse> verifyConsultation(
            @PathVariable String consultationId,
            @Valid @RequestBody VerifyConsultationRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        String userEmail = principal.getUsername();
        UUID uuid = parseUuid(consultationId);
        VerifyConsultationResponse response =
                consultationService.verifyConsultation(uuid, request, userEmail);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────

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
