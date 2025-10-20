package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.CreatePaymentRequest;
import com.hub.app.consultation.dto.PaymentResponse;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.consultation.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ConsultationJwtService jwtService;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> createPayment(
            @RequestParam UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreatePaymentRequest request) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);

            PaymentResponse response = paymentService.createPayment(consultationId, request, homeOwnerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getPayments(
            @RequestParam UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);

            List<PaymentResponse> responses = paymentService.getPaymentsByConsultation(consultationId, userId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }
}