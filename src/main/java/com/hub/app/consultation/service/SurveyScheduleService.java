package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.CreateSurveyScheduleRequest;
import com.hub.app.consultation.dto.SurveyScheduleResponse;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.model.SurveySchedule;
import com.hub.app.consultation.model.SurveyStatus;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.consultation.repository.SurveyScheduleRepository;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyScheduleService {

    private final SurveyScheduleRepository surveyScheduleRepository;
    private final ConsultationRepository consultationRepository;

    @PreAuthorize("hasRole('CONSULTANT')")
    @Transactional
    public SurveyScheduleResponse createSurveySchedule(UUID consultationId, CreateSurveyScheduleRequest request, Long consultantId) {
        Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (consultation.getStatus() != ConsultationStatus.MENUNGGU_JADWAL_SURVEY) {
            throw new DomainException(ErrorCode.INVALID_STATUS);
        }

        SurveySchedule surveySchedule = new SurveySchedule();
        surveySchedule.setConsultation(consultation);
        surveySchedule.setProposedDateTime(request.getProposedDateTime());
        surveySchedule.setNotes(request.getNotes());
        surveySchedule.setStatus(SurveyStatus.PENDING);

        SurveySchedule saved = surveyScheduleRepository.save(surveySchedule);

        // Update consultation status
        consultation.setStatus(ConsultationStatus.MENUNGGU_APPROVAL_JADWAL);
        consultationRepository.save(consultation);

        return mapToResponse(saved);
    }

    @PreAuthorize("hasRole('HOME_OWNER')")
    @Transactional
    public SurveyScheduleResponse approveSurveySchedule(UUID consultationId, Long homeOwnerId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, homeOwnerId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        SurveySchedule surveySchedule = surveyScheduleRepository.findByConsultationIdAndStatus(consultationId, SurveyStatus.PENDING)
                .orElseThrow(() -> new DomainException(ErrorCode.SURVEY_SCHEDULE_NOT_FOUND));

        surveySchedule.setStatus(SurveyStatus.APPROVED);
        SurveySchedule saved = surveyScheduleRepository.save(surveySchedule);

        // Update consultation status
        consultation.setStatus(ConsultationStatus.SURVEY_DIJADWALKAN);
        consultationRepository.save(consultation);

        return mapToResponse(saved);
    }

    @PreAuthorize("hasRole('HOME_OWNER')")
    @Transactional
    public SurveyScheduleResponse rejectSurveySchedule(UUID consultationId, Long homeOwnerId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, homeOwnerId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        SurveySchedule surveySchedule = surveyScheduleRepository.findByConsultationIdAndStatus(consultationId, SurveyStatus.PENDING)
                .orElseThrow(() -> new DomainException(ErrorCode.SURVEY_SCHEDULE_NOT_FOUND));

        surveySchedule.setStatus(SurveyStatus.REJECTED);
        SurveySchedule saved = surveyScheduleRepository.save(surveySchedule);

        // Reset consultation status to allow consultant to propose new schedule
        consultation.setStatus(ConsultationStatus.MENUNGGU_JADWAL_SURVEY);
        consultationRepository.save(consultation);

        return mapToResponse(saved);
    }

    @PreAuthorize("hasRole('HOME_OWNER')")
    @Transactional
    public SurveyScheduleResponse proposeNewSchedule(UUID consultationId, CreateSurveyScheduleRequest request, Long homeOwnerId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, homeOwnerId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        // Mark existing schedule as rescheduled
        surveyScheduleRepository.findByConsultationIdAndStatus(consultationId, SurveyStatus.PENDING)
                .ifPresent(schedule -> {
                    schedule.setStatus(SurveyStatus.RESCHEDULED);
                    surveyScheduleRepository.save(schedule);
                });

        // Create new schedule proposal
        SurveySchedule newSchedule = new SurveySchedule();
        newSchedule.setConsultation(consultation);
        newSchedule.setProposedDateTime(request.getProposedDateTime());
        newSchedule.setNotes(request.getNotes());
        newSchedule.setStatus(SurveyStatus.PENDING);

        SurveySchedule saved = surveyScheduleRepository.save(newSchedule);

        return mapToResponse(saved);
    }

    @PreAuthorize("hasRole('HOME_OWNER')")
    @Transactional
    public void completeSurvey(UUID consultationId, Long homeOwnerId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, homeOwnerId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (consultation.getStatus() != ConsultationStatus.SURVEY_DIJADWALKAN) {
            throw new DomainException(ErrorCode.INVALID_STATUS);
        }

        // Mark survey schedule as completed
        surveyScheduleRepository.findByConsultationIdAndStatus(consultationId, SurveyStatus.APPROVED)
                .ifPresent(schedule -> {
                    schedule.setStatus(SurveyStatus.COMPLETED);
                    surveyScheduleRepository.save(schedule);
                });

        // Update consultation status
        consultation.setStatus(ConsultationStatus.SURVEY_SELESAI);
        consultationRepository.save(consultation);
    }

    public List<SurveyScheduleResponse> getSurveySchedules(UUID consultationId, Long userId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        // Check if user is part of this consultation
        if (!consultation.getHomeOwner().getId().equals(userId) && !consultation.getConsultant().getId().equals(userId)) {
            throw new DomainException(ErrorCode.UNAUTHORIZED);
        }

        return surveyScheduleRepository.findByConsultationIdOrderByCreatedAtDesc(consultationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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