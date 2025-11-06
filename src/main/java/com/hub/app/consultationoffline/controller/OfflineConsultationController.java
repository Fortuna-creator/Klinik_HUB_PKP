package com.hub.app.consultationoffline.controller;

import com.hub.app.balaioffline.dto.ApiResponse;
import com.hub.app.consultationoffline.dto.CreateOfflineConsultationRequest;
import com.hub.app.consultationoffline.dto.OfflineConsultationResponse;
import com.hub.app.consultationoffline.dto.UpdateConsultationStatusRequest;
import com.hub.app.consultationoffline.service.OfflineConsultationService;
import com.hub.app.error.DomainException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}

