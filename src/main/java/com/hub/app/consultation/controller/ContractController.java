package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.ContractResponse;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.consultation.service.ContractService;
import com.hub.app.error.ApiErrorBuilder;
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

    @GetMapping("/{consultationId}")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getActiveContract(@PathVariable UUID consultationId) {
        try {
            ContractResponse response = contractService.getActiveContract(consultationId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }
}