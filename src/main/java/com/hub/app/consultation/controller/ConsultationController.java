package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.ConsultationResponse;
import com.hub.app.consultation.dto.CreateConsultationRequest;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.consultation.service.ConsultationService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
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
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private static final Logger logger = LoggerFactory.getLogger(ConsultationController.class);

    private final ConsultationService consultationService;
    private final ConsultationJwtService jwtService;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> createConsultation(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateConsultationRequest request) {
        logger.info("Creating consultation for homeOwnerId: {}, consultantId: {}, projectId: {}",
                   jwtService.extractUserId(authHeader), request.getConsultantId(), request.getProjectId());

        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            ConsultationResponse response = consultationService.createConsultation(homeOwnerId, request);
            logger.info("Consultation created successfully with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DomainException e) {
            logger.warn("Domain exception during consultation creation: {}", e.getMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(),
                                        e.getErrorCode().getCode(),
                                        e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during consultation creation", e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        "An unexpected error occurred while creating the consultation");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getConsultations(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            String role = jwtService.extractUserRole(authHeader);

            logger.info("Retrieving consultations for userId: {} with role: {}", userId, role);

            List<ConsultationResponse> responses;
            if ("HOME_OWNER".equals(role)) {
                responses = consultationService.getConsultationsByHomeOwner(userId);
                logger.info("Retrieved {} consultations for home owner: {}", responses.size(), userId);
            } else {
                responses = consultationService.getConsultationsByConsultant(userId);
                logger.info("Retrieved {} consultations for consultant: {}", responses.size(), userId);
            }

            return ResponseEntity.status(HttpStatus.OK).body(responses);
        } catch (DomainException e) {
            logger.warn("Domain exception during consultation retrieval: {}", e.getMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(),
                                        e.getErrorCode().getCode(),
                                        e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during consultation retrieval", e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        "An unexpected error occurred while retrieving consultations");
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getConsultation(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            String role = jwtService.extractUserRole(authHeader);
            boolean isHomeOwner = "HOME_OWNER".equals(role);

            logger.info("Retrieving consultation with ID: {} for userId: {} (role: {})", id, userId, role);

            ConsultationResponse response = consultationService.getConsultationById(id, userId, isHomeOwner);
            logger.info("Consultation retrieved successfully: {}", id);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            logger.warn("Domain exception during consultation retrieval for ID {}: {}", id, e.getMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(),
                                        e.getErrorCode().getCode(),
                                        e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during consultation retrieval for ID: {}", id, e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        "An unexpected error occurred while retrieving the consultation");
        }
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> acceptConsultation(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long consultantId = jwtService.extractUserId(authHeader);
            logger.info("Consultant {} attempting to accept consultation: {}", consultantId, id);

            ConsultationResponse response = consultationService.acceptConsultation(id, consultantId);
            logger.info("Consultation {} accepted successfully by consultant: {}", id, consultantId);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            logger.warn("Domain exception during consultation acceptance for ID {}: {}", id, e.getMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(),
                                        e.getErrorCode().getCode(),
                                        e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during consultation acceptance for ID: {}", id, e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        "An unexpected error occurred while accepting the consultation");
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> rejectConsultation(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long consultantId = jwtService.extractUserId(authHeader);
            logger.info("Consultant {} attempting to reject consultation: {}", consultantId, id);

            ConsultationResponse response = consultationService.rejectConsultation(id, consultantId);
            logger.info("Consultation {} rejected successfully by consultant: {}", id, consultantId);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            logger.warn("Domain exception during consultation rejection for ID {}: {}", id, e.getMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(),
                                        e.getErrorCode().getCode(),
                                        e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during consultation rejection for ID: {}", id, e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        "An unexpected error occurred while rejecting the consultation");
        }
    }

    @PatchMapping("/{id}/finalize")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> finalizeConsultation(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            logger.info("Home owner {} attempting to finalize consultation: {}", homeOwnerId, id);

            ConsultationResponse response = consultationService.finalizeConsultation(id, homeOwnerId);
            logger.info("Consultation {} finalized successfully by home owner: {}", id, homeOwnerId);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            logger.warn("Domain exception during consultation finalization for ID {}: {}", id, e.getMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(),
                                        e.getErrorCode().getCode(),
                                        e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during consultation finalization for ID: {}", id, e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        "An unexpected error occurred while finalizing the consultation");
        }
    }

    @PostMapping("/{id}/start-active")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> startActiveConsultation(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            logger.info("Home owner {} attempting to start active consultation: {}", homeOwnerId, id);

            ConsultationResponse response = consultationService.startActiveConsultation(id, homeOwnerId);
            logger.info("Active consultation {} started successfully by home owner: {}", id, homeOwnerId);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            logger.warn("Domain exception during active consultation start for ID {}: {}", id, e.getMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(),
                                        e.getErrorCode().getCode(),
                                        e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during active consultation start for ID: {}", id, e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        "An unexpected error occurred while starting the active consultation");
        }
    }

    @PostMapping("/{id}/start-revision")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> startRevision(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String notes) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            logger.info("Home owner {} attempting to start revision for consultation: {} with notes: {}", homeOwnerId, id, notes);

            ConsultationResponse response = consultationService.startRevision(id, homeOwnerId, notes);
            logger.info("Revision started successfully for consultation {} by home owner: {}", id, homeOwnerId);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            logger.warn("Domain exception during revision start for ID {}: {}", id, e.getMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(),
                                        e.getErrorCode().getCode(),
                                        e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during revision start for ID: {}", id, e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        "An unexpected error occurred while starting the revision");
        }
    }

    @PostMapping("/{id}/finalize-workflow")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> finalizeWorkflowConsultation(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            logger.info("Home owner {} attempting to finalize workflow for consultation: {}", homeOwnerId, id);

            ConsultationResponse response = consultationService.finalizeWorkflowConsultation(id, homeOwnerId);
            logger.info("Workflow finalized successfully for consultation {} by home owner: {}", id, homeOwnerId);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            logger.warn("Domain exception during workflow finalization for ID {}: {}", id, e.getMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(),
                                        e.getErrorCode().getCode(),
                                        e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during workflow finalization for ID: {}", id, e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        "An unexpected error occurred while finalizing the workflow");
        }
    }
}