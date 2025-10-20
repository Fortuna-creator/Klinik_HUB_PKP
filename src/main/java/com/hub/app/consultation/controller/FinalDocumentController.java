package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.FinalDocumentResponse;
import com.hub.app.consultation.service.FinalDocumentService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/final-documents")
@RequiredArgsConstructor
public class FinalDocumentController {

    private final FinalDocumentService finalDocumentService;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> uploadFinalDocument(
            @RequestParam UUID consultationId,
            @RequestParam String fileDedUrl,
            @RequestParam String fileRabUrl,
            @RequestParam String fileSpektekUrl,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long consultantId = extractUserIdFromToken(authHeader);

            FinalDocumentResponse response = finalDocumentService.uploadFinalDocument(
                    consultationId, fileDedUrl, fileRabUrl, fileSpektekUrl, consultantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> downloadFinalDocument(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = extractUserIdFromToken(authHeader);

            // This would return presigned URLs for download
            return ResponseEntity.ok("Download URLs would be generated here");
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> archiveFinalDocument(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long consultantId = extractUserIdFromToken(authHeader);

            FinalDocumentResponse response = finalDocumentService.archiveFinalDocument(id, consultantId);
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
}