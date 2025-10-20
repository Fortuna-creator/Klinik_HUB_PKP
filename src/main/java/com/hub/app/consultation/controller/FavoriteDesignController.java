package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.FavoriteDesignRequest;
import com.hub.app.consultation.dto.FavoriteDesignResponse;
import com.hub.app.consultation.service.FavoriteDesignService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations/favorites")
@RequiredArgsConstructor
public class FavoriteDesignController {

    private final FavoriteDesignService favoriteDesignService;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> toggleFavorite(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody FavoriteDesignRequest request) {
        try {
            Long homeOwnerId = extractUserIdFromToken(authHeader);

            FavoriteDesignResponse response = favoriteDesignService.toggleFavorite(homeOwnerId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> getFavorites(@RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = extractUserIdFromToken(authHeader);

            List<FavoriteDesignResponse> responses = favoriteDesignService.getFavoritesByHomeOwner(homeOwnerId);
            return ResponseEntity.ok(responses);
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
}