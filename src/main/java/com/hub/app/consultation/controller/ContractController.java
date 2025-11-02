package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.ContractGenerationRequest;
import com.hub.app.consultation.dto.ContractResponse;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.consultation.service.ContractService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Slf4j
public class ContractController {

    private final ContractService contractService;
    private final ConsultationJwtService jwtService;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping(value = "/consultations/{consultationId}/draft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> createContractDraft(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("request") ContractGenerationRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        log.info("Creating contract draft for consultationId: {}", consultationId);
        try {
            Long consultantId = jwtService.extractUserId(authHeader);
            log.debug("Extracted consultantId: {} from auth header", consultantId);


            ContractResponse response = contractService.createContractDraft(consultationId, consultantId, request, file);
            log.info("Contract draft created successfully for consultationId: {}", consultationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DomainException e) {
            log.warn("Domain exception while creating contract draft for consultationId: {}, error: {}", consultationId, e.getErrorCode().getDefaultMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            log.error("Unexpected error while creating contract draft for consultationId: {}", consultationId, e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                          ErrorCode.INTERNAL_ERROR.getCode(),
                                          e.getMessage());
        }
    }

    @PostMapping("/{contractId}/request-approval")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> requestContractApproval(
            @PathVariable UUID contractId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long consultantId = jwtService.extractUserId(authHeader);
            ContractResponse response = contractService.requestContractApproval(contractId, consultantId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @PostMapping("/{contractId}/approve")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> approveContract(
            @PathVariable UUID contractId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            ContractResponse response = contractService.approveContract(contractId, homeOwnerId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @PostMapping("/{contractId}/reject")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> rejectContract(
            @PathVariable UUID contractId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String reason) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            ContractResponse response = contractService.rejectContract(contractId, homeOwnerId, reason);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                          ErrorCode.INTERNAL_ERROR.getCode(),
                                          e.getMessage());
        }
    }

    @PostMapping("/{contractId}/request-revision")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> requestContractRevision(
            @PathVariable UUID contractId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String revisionNotes) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            ContractResponse response = contractService.requestContractRevision(contractId, homeOwnerId, revisionNotes);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                          ErrorCode.INTERNAL_ERROR.getCode(),
                                          e.getMessage());
        }
    }

    @GetMapping("/consultations/{consultationId}")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getActiveContract(@PathVariable UUID consultationId, @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            ContractResponse response = contractService.getContract(consultationId, userId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @PostMapping("/{consultationId}/generate-draft")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> generateContractDraft(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ContractGenerationRequest request) {
        log.info("Generating contract draft for consultationId: {}, request: {}", consultationId, request);
        try {
            Long consultantId = jwtService.extractUserId(authHeader);
            log.debug("Extracted consultantId: {} from auth header", consultantId);
            byte[] contractDocument = contractService.generateContractDraft(consultationId, consultantId, request);
            log.info("Contract draft generated successfully for consultationId: {}", consultationId);
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    .header("Content-Disposition", "attachment; filename=\"contract-draft.docx\"")
                    .body(contractDocument);
        } catch (DomainException e) {
            log.warn("Domain exception while generating contract draft for consultationId: {}, error: {}", consultationId, e.getErrorCode().getDefaultMessage());
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            log.error("Unexpected error while generating contract draft for consultationId: {}", consultationId, e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                          ErrorCode.INTERNAL_ERROR.getCode(),
                                          e.getMessage());
        }
    }

    @PatchMapping("/{id}/sign")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> signContract(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);

            ContractResponse response = contractService.signContract(id, userId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }
}