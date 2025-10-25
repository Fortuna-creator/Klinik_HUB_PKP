package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.CreateRevisionRequest;
import com.hub.app.consultation.dto.RevisionResponse;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.consultation.service.RevisionService;
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
@RequestMapping("/api/revisions")
@RequiredArgsConstructor
public class RevisionController {

    private final RevisionService revisionService;
    private final ConsultationJwtService jwtService;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> createRevision(
            @RequestParam UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateRevisionRequest request) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);

            RevisionResponse response = revisionService.createRevision(consultationId, request, homeOwnerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getRevisions(
            @RequestParam UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);

            List<RevisionResponse> responses = revisionService.getRevisionsByConsultation(consultationId, userId);
            return ResponseEntity.status(HttpStatus.OK).body(responses);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }
}