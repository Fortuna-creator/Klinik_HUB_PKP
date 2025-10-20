package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.ConsultationResponse;
import com.hub.app.consultation.dto.CreateConsultationRequest;
import com.hub.app.consultation.service.ConsultationService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> createConsultation(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateConsultationRequest request) {
        try {
            // Extract user ID from JWT token (simplified - in real implementation, use JWT service)
            Long homeOwnerId = extractUserIdFromToken(authHeader);

            ConsultationResponse response = consultationService.createConsultation(homeOwnerId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getConsultations(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            String role = extractRoleFromToken(authHeader);

            List<ConsultationResponse> responses;
            if ("HOME_OWNER".equals(role)) {
                responses = consultationService.getConsultationsByHomeOwner(userId);
            } else {
                responses = consultationService.getConsultationsByConsultant(userId);
            }

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getConsultation(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            String role = extractRoleFromToken(authHeader);
            boolean isHomeOwner = "HOME_OWNER".equals(role);

            ConsultationResponse response = consultationService.getConsultationById(id, userId, isHomeOwner);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @PatchMapping("/{id}/finalize")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> finalizeConsultation(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = extractUserIdFromToken(authHeader);

            ConsultationResponse response = consultationService.finalizeConsultation(id, homeOwnerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    // Helper methods (in real implementation, use JWT service)
    private Long extractUserIdFromToken(String authHeader) {
        // Simplified - extract from JWT
        return 1L; // Placeholder
    }

    private String extractRoleFromToken(String authHeader) {
        // Simplified - extract from JWT
        return "HOME_OWNER"; // Placeholder
    }
}