package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.DesignResponse;
import com.hub.app.consultation.service.DesignService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/consultations/designs")
@RequiredArgsConstructor
public class DesignController {

    private final DesignService designService;
    private final ApiErrorBuilder apiErrorBuilder;

    @GetMapping
    public ResponseEntity<?> getPrototypeDesigns(@RequestParam(required = false) String type) {
        try {
            List<DesignResponse> designs = designService.getPrototypeDesigns();
            return ResponseEntity.ok(designs);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDesign(@PathVariable UUID id) {
        try {
            DesignResponse design = designService.getDesignById(id);
            return ResponseEntity.ok(design);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    // Note: Design selection is not available for consultation flow
    // This endpoint is only for prototype designs in the design catalog

    @GetMapping("/{id}/download/{type}")
    public ResponseEntity<?> downloadDocument(
            @PathVariable UUID id,
            @PathVariable String type) {
        try {
            // Implementation for downloading documents via MinIO
            // This would use MinioService to generate presigned URLs
            return ResponseEntity.ok("Download URL would be generated here");
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping("/{id}/simbg")
    public ResponseEntity<?> getSimbgLink(@PathVariable UUID id) {
        try {
            // Implementation for SIMBG integration
            return ResponseEntity.ok("SIMBG link would be generated here");
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }
}