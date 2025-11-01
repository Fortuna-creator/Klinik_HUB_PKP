package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.CreateSurveyScheduleRequest;
import com.hub.app.consultation.dto.SurveyScheduleResponse;
import com.hub.app.consultation.event.ConsultationEvent;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.model.SurveySchedule;
import com.hub.app.consultation.model.SurveyStatus;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.consultation.repository.SurveyScheduleRepository;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyScheduleService {

    private final SurveyScheduleRepository surveyScheduleRepository;
    private final ConsultationRepository consultationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ConsultationHistoryService historyService;

    @PreAuthorize("hasRole('CONSULTANT')")
    @Transactional
    public SurveyScheduleResponse createSurveySchedule(UUID consultationId, CreateSurveyScheduleRequest request, Long consultantId) {
        log.info("=== STARTING SURVEY SCHEDULE CREATION ===");
        log.info("Consultation ID: {}, Consultant ID: {}", consultationId, consultantId);

        try {
            Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                    .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));
            log.debug("Consultation found: ID={}, Status={}", consultation.getId(), consultation.getStatus());

            if (consultation.getStatus() != ConsultationStatus.MENUNGGU_JADWAL_SURVEY) {
                log.error("Invalid consultation status for survey schedule creation: expected={}, actual={}",
                         ConsultationStatus.MENUNGGU_JADWAL_SURVEY, consultation.getStatus());
                throw new DomainException(ErrorCode.INVALID_STATUS);
            }

            SurveySchedule surveySchedule = new SurveySchedule();
            surveySchedule.setConsultation(consultation);
            surveySchedule.setProposedDateTime(request.getProposedDateTime());
            surveySchedule.setNotes(request.getNotes());
            surveySchedule.setStatus(SurveyStatus.PENDING);
            surveySchedule.setSurveyCost(request.getSurveyCost());

            SurveySchedule saved = surveyScheduleRepository.save(surveySchedule);
            log.debug("Survey schedule created: ID={}", saved.getId());

            // Update consultation status
            consultation.setStatus(ConsultationStatus.MENUNGGU_APPROVAL_JADWAL);
            consultationRepository.save(consultation);
            log.info("Consultation status updated to MENUNGGU_APPROVAL_JADWAL");

            // Publish event for history tracking
            eventPublisher.publishEvent(new ConsultationEvent(
                this, consultation, "SURVEY_SCHEDULED", "SURVEY", "SCHEDULED", "CONSULTANT",
                null, null, saved.getSurveyCost(), saved.getSurveyLocation(), saved.getId()
            ));
            log.info("Survey schedule creation event published");

            log.info("=== SURVEY SCHEDULE CREATION SUCCESSFUL ===");
            return mapToResponse(saved);
        } catch (DomainException e) {
            log.error("=== SURVEY SCHEDULE CREATION FAILED - DOMAIN EXCEPTION ===");
            log.error("Consultation ID: {}, Consultant ID: {}, Error Code: {}, Message: {}",
                     consultationId, consultantId, e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== SURVEY SCHEDULE CREATION FAILED - UNEXPECTED ERROR ===");
            log.error("Consultation ID: {}, Consultant ID: {}, Error: {}", consultationId, consultantId, e.getMessage(), e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('HOME_OWNER')")
    @Transactional
    public SurveyScheduleResponse approveSurveySchedule(UUID consultationId, Long homeOwnerId) {
        log.info("=== STARTING SURVEY SCHEDULE APPROVAL ===");
        log.info("Consultation ID: {}, Home Owner ID: {}", consultationId, homeOwnerId);

        try {
            Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, homeOwnerId)
                    .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));
            log.debug("Consultation found: ID={}, Status={}", consultation.getId(), consultation.getStatus());

            SurveySchedule surveySchedule = surveyScheduleRepository.findByConsultationIdAndStatus(consultationId, SurveyStatus.PENDING)
                    .orElseThrow(() -> new DomainException(ErrorCode.SURVEY_SCHEDULE_NOT_FOUND));
            log.debug("Pending survey schedule found: ID={}", surveySchedule.getId());

            surveySchedule.setStatus(SurveyStatus.APPROVED);
            SurveySchedule saved = surveyScheduleRepository.save(surveySchedule);
            log.info("Survey schedule approved: ID={}", saved.getId());

            // Update consultation status
            consultation.setStatus(ConsultationStatus.SURVEY_DIJADWALKAN);
            consultationRepository.save(consultation);
            log.info("Consultation status updated to SURVEY_DIJADWALKAN");

            // Publish event for history tracking
            eventPublisher.publishEvent(new ConsultationEvent(
                this, consultation, "SURVEY_APPROVED", "SURVEY", "APPROVED", "HOMEOWNER",
                null, null, surveySchedule.getSurveyCost(), surveySchedule.getSurveyLocation(), surveySchedule.getId()
            ));
            log.info("Survey approval event published");

            log.info("=== SURVEY SCHEDULE APPROVAL SUCCESSFUL ===");
            return mapToResponse(saved);
        } catch (DomainException e) {
            log.error("=== SURVEY SCHEDULE APPROVAL FAILED - DOMAIN EXCEPTION ===");
            log.error("Consultation ID: {}, Home Owner ID: {}, Error Code: {}, Message: {}",
                     consultationId, homeOwnerId, e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== SURVEY SCHEDULE APPROVAL FAILED - UNEXPECTED ERROR ===");
            log.error("Consultation ID: {}, Home Owner ID: {}, Error: {}", consultationId, homeOwnerId, e.getMessage(), e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('HOME_OWNER')")
    @Transactional
    public SurveyScheduleResponse rejectSurveySchedule(UUID consultationId, Long homeOwnerId) {
        log.info("=== STARTING SURVEY SCHEDULE REJECTION ===");
        log.info("Consultation ID: {}, Home Owner ID: {}", consultationId, homeOwnerId);

        try {
            Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, homeOwnerId)
                    .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));
            log.debug("Consultation found: ID={}, Status={}", consultation.getId(), consultation.getStatus());

            SurveySchedule surveySchedule = surveyScheduleRepository.findByConsultationIdAndStatus(consultationId, SurveyStatus.PENDING)
                    .orElseThrow(() -> new DomainException(ErrorCode.SURVEY_SCHEDULE_NOT_FOUND));
            log.debug("Pending survey schedule found: ID={}", surveySchedule.getId());

            surveySchedule.setStatus(SurveyStatus.REJECTED);
            SurveySchedule saved = surveyScheduleRepository.save(surveySchedule);
            log.info("Survey schedule rejected: ID={}", saved.getId());

            // Reset consultation status to allow consultant to propose new schedule
            consultation.setStatus(ConsultationStatus.MENUNGGU_JADWAL_SURVEY);
            consultationRepository.save(consultation);
            log.info("Consultation status reset to MENUNGGU_JADWAL_SURVEY");

            // Publish event for history tracking
            eventPublisher.publishEvent(new ConsultationEvent(
                this, consultation, "SURVEY_REJECTED", "SURVEY", "REJECTED", "HOMEOWNER",
                null, null, surveySchedule.getSurveyCost(), surveySchedule.getSurveyLocation(), surveySchedule.getId()
            ));
            log.info("Survey rejection event published");

            log.info("=== SURVEY SCHEDULE REJECTION SUCCESSFUL ===");
            return mapToResponse(saved);
        } catch (DomainException e) {
            log.error("=== SURVEY SCHEDULE REJECTION FAILED - DOMAIN EXCEPTION ===");
            log.error("Consultation ID: {}, Home Owner ID: {}, Error Code: {}, Message: {}",
                     consultationId, homeOwnerId, e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== SURVEY SCHEDULE REJECTION FAILED - UNEXPECTED ERROR ===");
            log.error("Consultation ID: {}, Home Owner ID: {}, Error: {}", consultationId, homeOwnerId, e.getMessage(), e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('CONSULTANT')")
    @Transactional
    public SurveyScheduleResponse proposeNewSchedule(UUID consultationId, CreateSurveyScheduleRequest request, Long consultantId) {
        log.info("=== STARTING NEW SURVEY SCHEDULE PROPOSAL ===");
        log.info("Consultation ID: {}, Consultant ID: {}", consultationId, consultantId);

        try {
            Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                    .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));
            log.debug("Consultation found: ID={}, Status={}", consultation.getId(), consultation.getStatus());

            // Mark existing schedule as rescheduled
            surveyScheduleRepository.findByConsultationIdAndStatus(consultationId, SurveyStatus.PENDING)
                    .ifPresent(schedule -> {
                        log.debug("Marking existing schedule as rescheduled: ID={}", schedule.getId());
                        schedule.setStatus(SurveyStatus.RESCHEDULED);
                        surveyScheduleRepository.save(schedule);
                        log.info("Existing schedule marked as RESCHEDULED");
                    });

            // Create new schedule proposal
            SurveySchedule newSchedule = new SurveySchedule();
            newSchedule.setConsultation(consultation);
            newSchedule.setProposedDateTime(request.getProposedDateTime());
            newSchedule.setNotes(request.getNotes());
            newSchedule.setStatus(SurveyStatus.PENDING);
            newSchedule.setSurveyCost(request.getSurveyCost());

            SurveySchedule saved = surveyScheduleRepository.save(newSchedule);
            log.debug("New survey schedule proposed: ID={}", saved.getId());

            // Update consultation status
            consultation.setStatus(ConsultationStatus.MENUNGGU_JADWAL_SURVEY);
            consultationRepository.save(consultation);
            log.info("Consultation status updated to MENUNGGU_JADWAL_SURVEY");

            // Publish event for history tracking
            eventPublisher.publishEvent(new ConsultationEvent(
                this, consultation, "SURVEY_RESCHEDULED", "SURVEY", "RESCHEDULED", "CONSULTANT",
                null, null, saved.getSurveyCost(), saved.getSurveyLocation(), saved.getId()
            ));
            log.info("New survey schedule proposal event published");

            log.info("=== NEW SURVEY SCHEDULE PROPOSAL SUCCESSFUL ===");
            return mapToResponse(saved);
        } catch (DomainException e) {
            log.error("=== NEW SURVEY SCHEDULE PROPOSAL FAILED - DOMAIN EXCEPTION ===");
            log.error("Consultation ID: {}, Consultant ID: {}, Error Code: {}, Message: {}",
                     consultationId, consultantId, e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== NEW SURVEY SCHEDULE PROPOSAL FAILED - UNEXPECTED ERROR ===");
            log.error("Consultation ID: {}, Consultant ID: {}, Error: {}", consultationId, consultantId, e.getMessage(), e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('CONSULTANT')")
    @Transactional
    public void completeSurvey(UUID consultationId, Long consultantId) {
        log.info("=== STARTING SURVEY COMPLETION ===");
        log.info("Consultation ID: {}, Consultant ID: {}", consultationId, consultantId);

        try {
            Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                    .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));
            log.debug("Consultation found: ID={}, Status={}", consultation.getId(), consultation.getStatus());

            if (consultation.getStatus() != ConsultationStatus.SURVEY_DIJADWALKAN) {
                log.error("Invalid consultation status for survey completion: expected={}, actual={}",
                         ConsultationStatus.SURVEY_DIJADWALKAN, consultation.getStatus());
                throw new DomainException(ErrorCode.INVALID_STATUS);
            }

            // Mark survey schedule as completed
            log.debug("Updating survey schedule status to COMPLETED");
            surveyScheduleRepository.findByConsultationIdAndStatus(consultationId, SurveyStatus.APPROVED)
                    .ifPresentOrElse(schedule -> {
                        log.debug("Found approved survey schedule: ID={}", schedule.getId());
                        schedule.setStatus(SurveyStatus.COMPLETED);
                        surveyScheduleRepository.save(schedule);
                        log.info("Survey schedule marked as COMPLETED");
                    }, () -> {
                        log.warn("No approved survey schedule found for consultation ID: {}", consultationId);
                    });

            // Update consultation status
            log.debug("Updating consultation status to SURVEY_SELESAI");
            consultation.setStatus(ConsultationStatus.SURVEY_SELESAI);
            consultationRepository.save(consultation);
            log.info("Consultation status updated to SURVEY_SELESAI");

            // Publish event for history tracking
            SurveySchedule completedSchedule = surveyScheduleRepository.findByConsultationIdAndStatus(consultationId, SurveyStatus.COMPLETED)
                    .orElse(null);
            log.debug("Completed survey schedule: {}", completedSchedule != null ? completedSchedule.getId() : "null");

            eventPublisher.publishEvent(new ConsultationEvent(
                this, consultation, "SURVEY_COMPLETED", "SURVEY", "COMPLETED", "CONSULTANT",
                java.util.Arrays.asList("https://hub-files.survey.photos.1.jpg", "https://hub-files.survey.photos.2.jpg"),
                java.util.Arrays.asList("https://hub-files.survey.report.pdf"),
                completedSchedule != null ? completedSchedule.getSurveyCost() : null,
                completedSchedule != null ? completedSchedule.getSurveyLocation() : null
            ));
            log.info("Survey completion event published");

            log.info("=== SURVEY COMPLETION SUCCESSFUL ===");
        } catch (DomainException e) {
            log.error("=== SURVEY COMPLETION FAILED - DOMAIN EXCEPTION ===");
            log.error("Consultation ID: {}, Consultant ID: {}, Error Code: {}, Message: {}",
                     consultationId, consultantId, e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== SURVEY COMPLETION FAILED - UNEXPECTED ERROR ===");
            log.error("Consultation ID: {}, Consultant ID: {}, Error: {}", consultationId, consultantId, e.getMessage(), e);
            throw e;
        }
    }

    public List<SurveyScheduleResponse> getSurveySchedules(UUID consultationId, Long userId) {
        log.info("=== RETRIEVING SURVEY SCHEDULES ===");
        log.info("Consultation ID: {}, User ID: {}", consultationId, userId);

        try {
            Consultation consultation = consultationRepository.findById(consultationId)
                    .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));
            log.debug("Consultation found: ID={}", consultation.getId());

            // Check if user is part of this consultation
            if (!consultation.getHomeOwner().getId().equals(userId) && !consultation.getConsultant().getId().equals(userId)) {
                log.error("Unauthorized access attempt: User ID {} not part of consultation ID {}", userId, consultationId);
                throw new DomainException(ErrorCode.UNAUTHORIZED);
            }

            List<SurveyScheduleResponse> schedules = surveyScheduleRepository.findByConsultationIdOrderByCreatedAtDesc(consultationId)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            log.info("Retrieved {} survey schedules for consultation ID {}", schedules.size(), consultationId);

            log.info("=== SURVEY SCHEDULES RETRIEVAL SUCCESSFUL ===");
            return schedules;
        } catch (DomainException e) {
            log.error("=== SURVEY SCHEDULES RETRIEVAL FAILED - DOMAIN EXCEPTION ===");
            log.error("Consultation ID: {}, User ID: {}, Error Code: {}, Message: {}",
                     consultationId, userId, e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== SURVEY SCHEDULES RETRIEVAL FAILED - UNEXPECTED ERROR ===");
            log.error("Consultation ID: {}, User ID: {}, Error: {}", consultationId, userId, e.getMessage(), e);
            throw e;
        }
    }

    private SurveyScheduleResponse mapToResponse(SurveySchedule surveySchedule) {
        return SurveyScheduleResponse.builder()
                .id(surveySchedule.getId())
                .consultationId(surveySchedule.getConsultation().getId())
                .proposedDateTime(surveySchedule.getProposedDateTime())
                .notes(surveySchedule.getNotes())
                .status(surveySchedule.getStatus())
                .createdAt(surveySchedule.getCreatedAt())
                .updatedAt(surveySchedule.getUpdatedAt())
                .build();
    }
}