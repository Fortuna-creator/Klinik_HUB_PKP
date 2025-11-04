package com.hub.app.payment.controller;

import com.hub.app.payment.dto.CreatePaymentRequest;
import com.hub.app.payment.dto.PaymentResponse;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.payment.service.PaymentService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
            return ResponseEntity.status(HttpStatus.OK).body(responses);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> requestPayment(
            @RequestParam UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long consultantId = jwtService.extractUserId(authHeader);

            paymentService.requestPayment(consultationId, consultantId);
            return ResponseEntity.status(HttpStatus.OK).body("Payment request sent to homeowner");
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @PostMapping("/{paymentId}/approve")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> approvePayment(
            @PathVariable UUID paymentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);

            PaymentResponse response = paymentService.approvePayment(paymentId, homeOwnerId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @PostMapping("/{paymentId}/release")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> releasePayment(
            @PathVariable UUID paymentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long consultantId = jwtService.extractUserId(authHeader);

            PaymentResponse response = paymentService.releasePayment(paymentId, consultantId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }
}