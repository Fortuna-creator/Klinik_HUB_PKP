package com.hub.app.offlineconsultation.controller;

import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.offlineconsultation.dto.response.AvailableSlotResponse;
import com.hub.app.offlineconsultation.dto.response.BalaiListItemResponse;
import com.hub.app.offlineconsultation.service.OfflineConsultationService;
import com.hub.app.offlineconsultation.service.SlotAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Balai (office) information endpoints.
 *
 * <p>Endpoints handled:</p>
 * <ul>
 *   <li>{@code GET /api/balai/list}                      — List all Balai offices</li>
 *   <li>{@code GET /api/balai/{balaiId}/available-slots}  — Available time slots for a date</li>
 * </ul>
 *
 * <p>Note: {@code GET /api/balai/list} is a public endpoint (no auth required).
 * {@code GET /api/balai/{balaiId}/available-slots} requires authentication.</p>
 */
@RestController
@RequestMapping("/api/balai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Balai Info", description = "Balai office information endpoints")
public class BalaiInfoController {

    private final OfflineConsultationService consultationService;
    private final SlotAvailabilityService slotAvailabilityService;

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/balai/list
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Returns a list of all Balai offices. This is a public endpoint —
     * no authentication required.
     */
    @GetMapping("/list")
    @Operation(summary = "Get all Balai offices")
    public ResponseEntity<List<BalaiListItemResponse>> getBalaiList() {
        List<BalaiListItemResponse> result = consultationService.getAllBalai();
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/balai/{balaiId}/available-slots
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Returns available time slots for a specific Balai on a given date.
     * Requires authentication.
     */
    @GetMapping("/{balaiId}/available-slots")
    @Operation(summary = "Get available time slots")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlots(
            @PathVariable String balaiId,
            @RequestParam String date) {

        UUID balaiUuid = parseUuid(balaiId);
        LocalDate localDate = parseDate(date);

        List<AvailableSlotResponse> slots =
                slotAvailabilityService.getAvailableSlots(balaiUuid, localDate);
        return ResponseEntity.ok(slots);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Parses a string as UUID.
     */
    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DomainException(ErrorCode.BAD_REQUEST,
                    "Invalid Balai ID format: " + value);
        }
    }

    /**
     * Parses a date string in ISO 8601 format (YYYY-MM-DD).
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new DomainException(ErrorCode.BAD_REQUEST,
                    "Invalid date format: " + dateStr + ". Expected YYYY-MM-DD");
        }
    }
}
