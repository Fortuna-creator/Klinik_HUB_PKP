package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.ContractResponse;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.consultation.service.ContractService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final ConsultationJwtService jwtService;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping("/consultations/{consultationId}/draft")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> createContractDraft(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam java.math.BigDecimal contractValue,
            @RequestParam String fileUrl,
            @RequestParam String termsConditionsUrl,
            @RequestParam String scopeOfWorkUrl) {
        try {
            Long consultantId = jwtService.extractUserId(authHeader);
            ContractResponse response = contractService.createContractDraft(consultationId, consultantId,
                    contractValue, fileUrl, termsConditionsUrl, scopeOfWorkUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
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