package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.CreateSurveyScheduleRequest;
import com.hub.app.consultation.dto.SurveyScheduleResponse;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.consultation.service.SurveyScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/survey-schedules")
@RequiredArgsConstructor
public class SurveyScheduleController {

    private final SurveyScheduleService surveyScheduleService;
    private final ConsultationJwtService jwtService;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping("/consultations/{consultationId}")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> createSurveySchedule(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateSurveyScheduleRequest request) {
        try {
            Long consultantId = jwtService.extractUserId(authHeader);
            SurveyScheduleResponse response = surveyScheduleService.createSurveySchedule(consultationId, request, consultantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @PostMapping("/consultations/{consultationId}/approve")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> approveSurveySchedule(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            SurveyScheduleResponse response = surveyScheduleService.approveSurveySchedule(consultationId, homeOwnerId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @PostMapping("/consultations/{consultationId}/reject")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> rejectSurveySchedule(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            SurveyScheduleResponse response = surveyScheduleService.rejectSurveySchedule(consultationId, homeOwnerId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @PostMapping("/consultations/{consultationId}/reschedule")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> proposeNewSchedule(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateSurveyScheduleRequest request) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            SurveyScheduleResponse response = surveyScheduleService.proposeNewSchedule(consultationId, request, homeOwnerId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @PostMapping("/consultations/{consultationId}/complete")
    @PreAuthorize("hasRole('HOME_OWNER')")
    public ResponseEntity<?> completeSurvey(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long homeOwnerId = jwtService.extractUserId(authHeader);
            surveyScheduleService.completeSurvey(consultationId, homeOwnerId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping("/consultations/{consultationId}")
    public ResponseEntity<?> getSurveySchedules(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            List<SurveyScheduleResponse> responses = surveyScheduleService.getSurveySchedules(consultationId, userId);
            return ResponseEntity.status(HttpStatus.OK).body(responses);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }
}