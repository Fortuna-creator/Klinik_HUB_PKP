package com.hub.app.balaioffline.controller;

import com.hub.app.balaioffline.dto.ApiResponse;
import com.hub.app.balaioffline.dto.BalaiRequest;
import com.hub.app.balaioffline.dto.BalaiResponse;
import com.hub.app.balaioffline.service.BalaiOfflineService;
import com.hub.app.error.DomainException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/offline/balai")
@RequiredArgsConstructor
public class BalaiOfflineController {

    private static final Logger logger = LoggerFactory.getLogger(BalaiOfflineController.class);
    private final BalaiOfflineService balaiOfllineService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BalaiResponse>>> getAllBalai() {
        try {
            logger.info("GET /api/offline/balai - Fetching all balai");
            List<BalaiResponse> balaiList = balaiOfllineService.getAllBalai();
            logger.info("Successfully retrieved {} balai records", balaiList.size());
            return ResponseEntity.ok(ApiResponse.success(balaiList));
        } catch (Exception e) {
            logger.error("Error fetching all balai", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch balai data."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BalaiResponse>> getBalaiById(@PathVariable UUID id) {
        try {
            logger.info("GET /api/offline/balai/{} - Fetching balai by id", id);
            BalaiResponse balai = balaiOfllineService.getBalaiById(id);
            logger.info("Successfully retrieved balai: {}", balai.getName());
            return ResponseEntity.ok(ApiResponse.success(balai));
        } catch (DomainException e) {
            logger.warn("Balai not found with id: {}", id);
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching balai by id: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch balai data."));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BalaiResponse>> createBalai(@Valid @RequestBody BalaiRequest request) {
        try {
            logger.info("POST /api/offline/balai - Creating new balai");
            BalaiResponse balai = balaiOfllineService.createBalai(request);
            logger.info("Successfully created balai with id: {}", balai.getId());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(balai));
        } catch (Exception e) {
            logger.error("Error creating balai", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create balai."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BalaiResponse>> updateBalai(
            @PathVariable UUID id,
            @Valid @RequestBody BalaiRequest request) {
        try {
            logger.info("PUT /api/offline/balai/{} - Updating balai", id);
            BalaiResponse balai = balaiOfllineService.updateBalai(id, request);
            logger.info("Successfully updated balai: {}", balai.getName());
            return ResponseEntity.ok(ApiResponse.success(balai));
        } catch (DomainException e) {
            logger.warn("Balai not found with id: {}", id);
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating balai with id: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update balai."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBalai(@PathVariable UUID id) {
        try {
            logger.info("DELETE /api/offline/balai/{} - Deleting balai", id);
            balaiOfllineService.deleteBalai(id);
            logger.info("Successfully deleted balai with id: {}", id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (DomainException e) {
            logger.warn("Balai not found with id: {}", id);
            return ResponseEntity
                    .status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting balai with id: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete balai."));
        }
    }
}

