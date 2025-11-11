package com.hub.app.consultationoffline.controller;

import com.hub.app.balaioffline.dto.ApiResponse;
import com.hub.app.consultationoffline.dto.*;
import com.hub.app.consultationoffline.service.OfflineConsultationService;
import com.hub.app.error.DomainException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/offline/balai/consultations")
@RequiredArgsConstructor
public class OfflineConsultationController {

    private static final Logger logger = LoggerFactory.getLogger(OfflineConsultationController.class);
    private final OfflineConsultationService consultationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OfflineConsultationResponse>> createConsultation(
            @Valid @RequestBody CreateOfflineConsultationRequest request) {
        try {
            logger.info("POST /api/offline/balai/consultations - Creating consultation for user {}", request.getUserId());
            OfflineConsultationResponse response = consultationService.createConsultation(request);
            logger.info("Successfully created consultation with id: {}", response.getId());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.<OfflineConsultationResponse>builder()
                            .status("success")
                            .message("Consultation created successfully.")
                            .data(response)
                            .build());
        } catch (DomainException e) {
            logger.warn("Failed to create consultation: {}", e.getMessage());
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.<OfflineConsultationResponse>builder()
                            .status("error")
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            logger.error("Error creating consultation", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<OfflineConsultationResponse>builder()
                            .status("error")
                            .message("Failed to create consultation.")
                            .build());
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OfflineConsultationResponse>>> getAllConsultations() {
        try {
            logger.info("GET /api/offline/balai/consultations - Fetching all consultations");
            List<OfflineConsultationResponse> consultations = consultationService.getAllConsultations();
            logger.info("Successfully retrieved {} consultations", consultations.size());
            return ResponseEntity.ok(ApiResponse.success(consultations));
        } catch (Exception e) {
            logger.error("Error fetching consultations", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve consultations."));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OfflineConsultationResponse>> getConsultationById(@PathVariable UUID id) {
        try {
            logger.info("GET /api/offline/balai/consultations/{} - Fetching consultation by id", id);
            OfflineConsultationResponse response = consultationService.getConsultationById(id);
            logger.info("Successfully retrieved consultation: {}", response.getId());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (DomainException e) {
            logger.warn("Consultation not found with id: {}", id);
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching consultation by id: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve consultation."));
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OfflineConsultationResponse>> updateConsultationStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateConsultationStatusRequest request) {
        try {
            logger.info("PATCH /api/offline/balai/consultations/{} - Updating status to {}", id, request.getStatus());
            OfflineConsultationResponse response = consultationService.updateConsultationStatus(id, request);
            logger.info("Successfully updated consultation status");
            return ResponseEntity.ok(
                    ApiResponse.<OfflineConsultationResponse>builder()
                            .status("success")
                            .message("Consultation status updated.")
                            .data(response)
                            .build());
        } catch (DomainException e) {
            logger.warn("Failed to update consultation: {}", e.getMessage());
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.<OfflineConsultationResponse>builder()
                            .status("error")
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            logger.error("Error updating consultation status", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<OfflineConsultationResponse>builder()
                            .status("error")
                            .message("Failed to update consultation status.")
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteConsultation(@PathVariable UUID id) {
        try {
            logger.info("DELETE /api/offline/balai/consultations/{} - Deleting consultation", id);
            consultationService.deleteConsultation(id);
            logger.info("Successfully deleted consultation");
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .status("success")
                            .message("Consultation deleted successfully.")
                            .build());
        } catch (DomainException e) {
            logger.warn("Consultation not found with id: {}", id);
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.<Void>builder()
                            .status("error")
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            logger.error("Error deleting consultation", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .status("error")
                            .message("Failed to delete consultation.")
                            .build());
        }
    }

    // ==================== NEW ENDPOINTS FOR CALENDAR FEATURE ====================

    /**
     * Get available time slots untuk tanggal tertentu
     * Endpoint: GET /api/offline/balai/consultations/available-slots?balaiId={uuid}&date=2025-11-15&interval=60
     * Used for: Calendar time picker
     */
    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<AvailableSlotsResponse>> getAvailableSlots(
            @RequestParam UUID balaiId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "60") Integer interval) {
        try {
            logger.info("GET /api/offline/balai/consultations/available-slots - balaiId: {}, date: {}", balaiId, date);
            AvailableSlotsResponse response = consultationService.getAvailableSlots(balaiId, date, interval);
            logger.info("Successfully retrieved available slots: {} available, {} booked", 
                    response.getTotalAvailableSlots(), response.getTotalBookedSlots());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (DomainException e) {
            logger.warn("Error getting available slots: {}", e.getMessage());
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching available slots", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve available slots."));
        }
    }

    /**
     * Get booked dates calendar untuk bulan tertentu
     * Endpoint: GET /api/offline/balai/consultations/booked-dates?balaiId={uuid}&month=11&year=2025
     * Used for: Calendar month view
     */
    @GetMapping("/booked-dates")
    public ResponseEntity<ApiResponse<BookedDatesCalendarResponse>> getBookedDatesCalendar(
            @RequestParam UUID balaiId,
            @RequestParam int month,
            @RequestParam int year) {
        try {
            logger.info("GET /api/offline/balai/consultations/booked-dates - balaiId: {}, month: {}, year: {}", 
                    balaiId, month, year);
            BookedDatesCalendarResponse response = consultationService.getBookedDatesCalendar(balaiId, month, year);
            logger.info("Successfully retrieved calendar: {} days with bookings, {} fully booked", 
                    response.getTotalDaysWithBookings(), response.getTotalFullyBookedDays());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (DomainException e) {
            logger.warn("Error getting booked dates: {}", e.getMessage());
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching booked dates calendar", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve booked dates calendar."));
        }
    }

    /**
     * Get user consultation history (upcoming & past)
     * Endpoint: GET /api/offline/balai/consultations/my-bookings
     * Used for: My Bookings page
     */
    @GetMapping("/my-bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserConsultationHistoryResponse>> getMyBookings(
            @RequestParam Long userId) {
        try {
            logger.info("GET /api/offline/balai/consultations/my-bookings - userId: {}", userId);
            UserConsultationHistoryResponse response = consultationService.getUserConsultationHistory(userId);
            logger.info("Successfully retrieved user bookings: {} upcoming, {} past", 
                    response.getTotalUpcoming(), response.getTotalPast());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (DomainException e) {
            logger.warn("Error getting user bookings: {}", e.getMessage());
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching user bookings", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve user bookings."));
        }
    }

    /**
     * Get daily schedule untuk balai tertentu
     * Endpoint: GET /api/offline/balai/consultations/daily-schedule?balaiId={uuid}&date=2025-11-15
     * Used for: Daily schedule view (admin)
     */
    @GetMapping("/daily-schedule")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DailyScheduleResponse>> getDailySchedule(
            @RequestParam UUID balaiId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            logger.info("GET /api/offline/balai/consultations/daily-schedule - balaiId: {}, date: {}", balaiId, date);
            DailyScheduleResponse response = consultationService.getDailySchedule(balaiId, date);
            logger.info("Successfully retrieved daily schedule: {} consultations, {} slots available", 
                    response.getTotalConsultations(), response.getAvailableSlots());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (DomainException e) {
            logger.warn("Error getting daily schedule: {}", e.getMessage());
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching daily schedule", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve daily schedule."));
        }
    }

    /**
     * Get consultations by user
     * Endpoint: GET /api/offline/balai/consultations/user/{userId}
     * Used for: User consultation list
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OfflineConsultationResponse>>> getConsultationsByUser(
            @PathVariable Long userId) {
        try {
            logger.info("GET /api/offline/balai/consultations/user/{} - Fetching consultations by user", userId);
            List<OfflineConsultationResponse> consultations = consultationService.getConsultationsByUserId(userId);
            logger.info("Successfully retrieved {} consultations for user", consultations.size());
            return ResponseEntity.ok(ApiResponse.success(consultations));
        } catch (DomainException e) {
            logger.warn("Error getting consultations by user: {}", e.getMessage());
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching consultations by user", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve user consultations."));
        }
    }

    /**
     * Get consultations by balai
     * Endpoint: GET /api/offline/balai/consultations/balai/{balaiId}
     * Used for: Balai consultation list (admin view)
     */
    @GetMapping("/balai/{balaiId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OfflineConsultationResponse>>> getConsultationsByBalai(
            @PathVariable UUID balaiId) {
        try {
            logger.info("GET /api/offline/balai/consultations/balai/{} - Fetching consultations by balai", balaiId);
            List<OfflineConsultationResponse> consultations = consultationService.getConsultationsByBalaiId(balaiId);
            logger.info("Successfully retrieved {} consultations for balai", consultations.size());
            return ResponseEntity.ok(ApiResponse.success(consultations));
        } catch (DomainException e) {
            logger.warn("Error getting consultations by balai: {}", e.getMessage());
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching consultations by balai", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve balai consultations."));
        }
    }
}

