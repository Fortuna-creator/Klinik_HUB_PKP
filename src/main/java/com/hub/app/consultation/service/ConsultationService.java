package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.ConsultationResponse;
import com.hub.app.consultation.dto.CreateConsultationRequest;
import com.hub.app.consultation.event.ConsultationEvent;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.consultation.service.ConsultationWorkflowService;
import com.hub.app.project.model.Project;
import com.hub.app.project.model.ProjectStatus;
import com.hub.app.project.repository.ProjectRepository;
import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.HomeOwner;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.ConsultantRepository;
import com.hub.app.user.repository.HomeOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final HomeOwnerRepository homeOwnerRepository;
    private final ConsultantRepository consultantRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ConsultationWorkflowService workflowService;

    public ConsultationResponse acceptConsultation(UUID consultationId, Long consultantId) {
        Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));

        if (consultation.getStatus() != ConsultationStatus.MENUNGGU_KONFIRMASI_KONSULTAN) {
            throw new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.INVALID_STATUS);
        }

        // Set project status to ACTIVE when consultation is accepted
        // Find the project that was set to PENDING for this consultation
        // Since consultation is linked to home owner, we need to find projects by home owner
        List<Project> pendingProjects = projectRepository.findByUserAndStatus(consultation.getHomeOwner(), ProjectStatus.PENDING);
        if (pendingProjects.isEmpty()) {
            throw new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.PROJECT_NOT_FOUND);
        }
        // For simplicity, assume the first pending project is the one
        Project project = pendingProjects.get(0);
        project.setStatus(ProjectStatus.ACTIVE);
        projectRepository.save(project);

        consultation.setStatus(ConsultationStatus.MENUNGGU_JADWAL_SURVEY);
        consultation = consultationRepository.save(consultation);

        // Publish event for history tracking
        eventPublisher.publishEvent(new ConsultationEvent(
            this, consultation, "CONSULTATION_ACCEPTED", "CONSULTATION", "ACCEPTED", "CONSULTANT"
        ));

        return mapToResponse(consultation);
    }

    public ConsultationResponse rejectConsultation(UUID consultationId, Long consultantId) {
        Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));

        if (consultation.getStatus() != ConsultationStatus.MENUNGGU_KONFIRMASI_KONSULTAN) {
            throw new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.INVALID_STATUS);
        }

        consultation.setStatus(ConsultationStatus.SELESAI); // Consultation rejected
        consultation = consultationRepository.save(consultation);

        // Publish event for history tracking
        eventPublisher.publishEvent(new ConsultationEvent(
            this, consultation, "CONSULTATION_CREATED", "CONSULTATION", "CREATED", "HOMEOWNER"
        ));

        return mapToResponse(consultation);
    }

    public ConsultationResponse createConsultation(Long homeOwnerId, CreateConsultationRequest request) {
        HomeOwner homeOwner = homeOwnerRepository.findById(homeOwnerId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.HOME_OWNER_NOT_FOUND));

        Consultant consultant = consultantRepository.findById(request.getConsultantId())
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findByProjectId(request.getProjectId())
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.PROJECT_NOT_FOUND));

        // Check if project belongs to home owner
        if (!project.getUser().getId().equals(homeOwnerId)) {
            throw new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.UNAUTHORIZED);
        }

        // Set project status to PENDING when consultation is created
        project.setStatus(ProjectStatus.PENDING);
        projectRepository.save(project);

        Consultation consultation = new Consultation();
        consultation.setHomeOwner(homeOwner);
        consultation.setConsultant(consultant);
        consultation.setConsultationType(request.getConsultationType());
        consultation.setChannel(request.getChannel());
        consultation.setStatus(ConsultationStatus.MENUNGGU_KONFIRMASI_KONSULTAN);
        consultation.setProjectId(project.getProjectId());

        consultation = consultationRepository.save(consultation);
        return mapToResponse(consultation);
    }

    public List<ConsultationResponse> getConsultationsByHomeOwner(Long homeOwnerId) {
        List<Consultation> consultations = consultationRepository.findByHomeOwnerId(homeOwnerId);
        return consultations.stream().map(this::mapToResponse).toList();
    }

    public List<ConsultationResponse> getConsultationsByConsultant(Long consultantId) {
        List<Consultation> consultations = consultationRepository.findByConsultantId(consultantId);
        return consultations.stream().map(this::mapToResponse).toList();
    }

    public ConsultationResponse getConsultationById(UUID id, Long userId, boolean isHomeOwner) {
        Consultation consultation;
        if (isHomeOwner) {
            consultation = consultationRepository.findByIdAndHomeOwnerId(id, userId)
                    .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));
        } else {
            consultation = consultationRepository.findByIdAndConsultantId(id, userId)
                    .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));
        }
        return mapToResponse(consultation);
    }

    public ConsultationResponse finalizeConsultation(UUID id, Long homeOwnerId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(id, homeOwnerId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));

        if (consultation.getStatus() != ConsultationStatus.FINAL) {
            throw new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.INVALID_STATUS);
        }

        consultation.setStatus(ConsultationStatus.SELESAI);
        consultation.setCompletedAt(Instant.now());
        consultation = consultationRepository.save(consultation);

        // Publish event for history tracking
        eventPublisher.publishEvent(new ConsultationEvent(
            this, consultation, "CONSULTATION_COMPLETED", "CONSULTATION", "COMPLETED", "HOMEOWNER"
        ));

        return mapToResponse(consultation);
    }

    /**
     * @deprecated Use contract signing to activate consultation instead
     */
    @Deprecated
    public ConsultationResponse startActiveConsultation(UUID consultationId, Long homeOwnerId) {
        throw new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.BAD_REQUEST, "Consultation activation is now handled through contract signing");
    }

    public ConsultationResponse startRevision(UUID consultationId, Long homeOwnerId, String revisionNotes) {
        workflowService.startRevision(consultationId, homeOwnerId, revisionNotes);
        return getConsultationById(consultationId, homeOwnerId, true);
    }

    public ConsultationResponse finalizeWorkflowConsultation(UUID consultationId, Long homeOwnerId) {
        workflowService.finalizeConsultation(consultationId, homeOwnerId);
        return getConsultationById(consultationId, homeOwnerId, true);
    }

    public ConsultationResponse mapToResponse(Consultation consultation) {
        return ConsultationResponse.builder()
                .id(consultation.getId())
                .homeOwnerId(consultation.getHomeOwner().getId())
                .homeOwnerName(consultation.getHomeOwner().getName())
                .consultantId(consultation.getConsultant().getId())
                .consultantName(consultation.getConsultant().getName())
                .designId(consultation.getDesign() != null ? consultation.getDesign().getId() : null)
                .designName(consultation.getDesign() != null ? consultation.getDesign().getName() : null)
                .consultationType(consultation.getConsultationType())
                .channel(consultation.getChannel())
                .status(consultation.getStatus())
                .startedAt(consultation.getStartedAt())
                .completedAt(consultation.getCompletedAt())
                .createdAt(consultation.getCreatedAt())
                .updatedAt(consultation.getUpdatedAt())
                .projectId(consultation.getProjectId())
                .build();
    }
}