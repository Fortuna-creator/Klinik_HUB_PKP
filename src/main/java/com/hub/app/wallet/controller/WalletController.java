package com.hub.app.wallet.controller;


import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import com.hub.app.wallet.dto.TopupRequest;
import com.hub.app.wallet.dto.WalletResponse;
import com.hub.app.wallet.model.UserWallet;
import com.hub.app.wallet.service.WalletService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final ApiErrorBuilder apiErrorBuilder;
    private final ConsultationJwtService jwtService;

    @GetMapping("/balance")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getBalance(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract user ID from JWT (assuming you have a JWT service)
            Long userId = extractUserIdFromToken(authHeader);

            UserWallet wallet = walletService.getWallet(userId);
            WalletResponse response = mapToResponse(wallet);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                       ErrorCode.INTERNAL_ERROR.getCode(),
                                       e.getMessage());
        }
    }

    @PostMapping("/topup")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> topupBalance(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TopupRequest request) {
        try {
            Long userId = extractUserIdFromToken(authHeader);

            UserWallet wallet = walletService.addBalance(userId, request.getAmount());
            WalletResponse response = mapToResponse(wallet);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                       ErrorCode.INTERNAL_ERROR.getCode(),
                                       e.getMessage());
        }
    }

    // Admin endpoint for simulation purposes
    @PostMapping("/admin/topup/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminTopup(
            @PathVariable Long userId,
            @Valid @RequestBody TopupRequest request) {
        try {
            UserWallet wallet = walletService.addBalance(userId, request.getAmount());
            WalletResponse response = mapToResponse(wallet);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                       ErrorCode.INTERNAL_ERROR.getCode(),
                                       e.getMessage());
        }
    }

    protected Long extractUserIdFromToken(String authHeader) {
        return jwtService.extractUserId(authHeader);
    }

    private WalletResponse mapToResponse(UserWallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUser() != null ? wallet.getUser().getId() : null)
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .isActive(wallet.getIsActive())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}