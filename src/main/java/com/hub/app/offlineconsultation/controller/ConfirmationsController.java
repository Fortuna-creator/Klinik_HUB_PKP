package com.hub.app.offlineconsultation.controller;

import com.hub.app.offlineconsultation.dto.response.BookingConfirmationResponse;
import com.hub.app.offlineconsultation.service.OfflineConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for booking confirmations and inbox operations.
 *
 * <p>Endpoints handled:</p>
 * <ul>
 *   <li>{@code GET  /api/confirmations/inbox} — Get booking confirmations for inbox</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/confirmations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Confirmations", description = "Booking confirmation and inbox endpoints")
public class ConfirmationsController {

    private final OfflineConsultationService consultationService;

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/confirmations/inbox
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Get the authenticated Warga's booking confirmations (confirmed consultations)
     * for the inbox page. Only returns consultations with status: terkonfirmasi, selesai, or sedang_berjalan.
     *
     * @param status Filter by status (default: "all")
     * @param page   Page number (default: 0)
     * @param size   Page size (default: 10)
     * @param principal Authenticated user details
     * @return List of booking confirmations
     */
    @GetMapping("/inbox")
    @Operation(summary = "Get booking confirmations for inbox")
    public ResponseEntity<List<BookingConfirmationResponse>> getBookingConfirmations(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails principal) {

        String userEmail = principal.getUsername();
        List<BookingConfirmationResponse> result =
                consultationService.getBookingConfirmations(userEmail, status, page, size);
        return ResponseEntity.ok(result);
    }
}
