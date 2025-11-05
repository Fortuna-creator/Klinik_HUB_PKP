package com.hub.app.project.service;

import com.hub.app.consultation.model.*;
import com.hub.app.consultation.repository.*;
import com.hub.app.consultation.service.*;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.permit.model.Permit;
import com.hub.app.permit.repository.PermitRepository;
import com.hub.app.project.dto.*;
import com.hub.app.project.model.Project;
import com.hub.app.project.model.ProjectStatus;
import com.hub.app.project.model.ProjectType;
import com.hub.app.project.repository.ProjectRepository;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ConsultationRepository consultationRepository;
    private final ConsultationHistoryService consultationHistoryService;
    private final SurveyScheduleRepository surveyScheduleRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ContractRepository contractRepository;
    private final PermitRepository permitRepository;
    private final RevisionRepository revisionRepository;

    @Transactional
    public CreateProjectResponse createProject(CreateProjectRequest request, User user) {
        Project project = Project.builder()
                .name(request.getName())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .locationDetail(request.getLocationDetail())
                .type(request.getType())
                .landArea(request.getLandArea())
                .income(request.getIncome())
                .user(user)
                .build();

        Project saved = projectRepository.save(project);

        log.info("Project created: {} for user: {}", saved.getProjectId(), user.getEmail());

        return CreateProjectResponse.builder()
                .projectId(saved.getProjectId())
                .name(saved.getName())
                .type(saved.getType().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public ProjectListWrapper getUserProjects(User user, Integer page, Integer size, ProjectType type, ProjectStatus status) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 10);

        Page<Project> projectsPage;

        if (user.getRole().equals(Role.CONSULTANT)) {
            List<Consultation> consultations = consultationRepository.findByConsultantId(user.getId());
            if(!consultations.isEmpty()) {
                projectsPage = projectRepository.findByProjectIdIn(consultations.stream().map(Consultation::getProjectId).toList(), pageable);
            } else {
                projectsPage = projectRepository.findByUser(user, pageable);
            }
        } else {
            if (type != null && status != null) {
                projectsPage = projectRepository.findByUserAndTypeAndStatus(user, type, status, pageable);
            } else if (type != null) {
                projectsPage = projectRepository.findByUserAndType(user, type, pageable);
            } else if (status != null) {
                projectsPage = projectRepository.findByUserAndStatus(user, status, pageable);
            } else {
                projectsPage = projectRepository.findByUser(user, pageable);
            }
        }

        List<ProjectListResponse> responses = projectsPage.getContent().stream()
                .map(this::mapToProjectListResponse)
                .collect(Collectors.toList());

        return ProjectListWrapper.builder()
                .projects(responses)
                .build();
    }

    public ProjectDetailResponse getProjectDetail(UUID projectId, User user) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new DomainException(ErrorCode.PROJECT_NOT_FOUND));

        // Check if project belongs to user or user is the assigned consultant
        if (!project.getUser().getId().equals(user.getId())) {
            // Check if user is consultant for this project's consultation
            Optional<Consultation> consultation = consultationRepository.findByHomeOwnerId(project.getUser().getId())
                    .stream()
                    .filter(c -> c.getConsultant().getId().equals(user.getId()))
                    .findFirst();
            if (consultation.isEmpty()) {
                throw new DomainException(ErrorCode.UNAUTHORIZED);
            }
        }

        return mapToProjectDetailResponse(project);
    }

    private ProjectListResponse mapToProjectListResponse(Project project) {
        return ProjectListResponse.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .type(project.getType().name())
                .status(project.getStatus().name())
                .location(ProjectListResponse.Location.builder()
                        .longitude(project.getLongitude())
                        .latitude(project.getLatitude())
                        .address(project.getLocationDetail())
                        .build())
                .landArea(project.getLandArea())
                .createdAt(project.getCreatedAt())
                .build();
    }

    private ProjectDetailResponse mapToProjectDetailResponse(Project project) {
        // Find consultation for this project (assuming one consultation per project)
        Optional<Consultation> consultationOpt = consultationRepository.findByProjectId(project.getProjectId());

        ProjectDetailResponse.ConsultationInfo consultationInfo = null;
        if (consultationOpt.isPresent()) {
            Consultation consultation = consultationOpt.get();
            consultationInfo = buildConsultationInfo(consultation);
        }

        // Get permit information
        ProjectDetailResponse.PermitInfo permitInfo = null;
        Optional<Permit> permitOpt = permitRepository.findByProjectId(project.getProjectId());
        if (permitOpt.isPresent()) {
            Permit permit = permitOpt.get();
            permitInfo = ProjectDetailResponse.PermitInfo.builder()
                    .permitId(permit.getId())
                    .permitType(permit.getPermitType())
                    .status(permit.getStatus().name())
                    .submittedAt(permit.getSubmittedAt())
                    .dedDocumentUrl(permit.getDedDocumentUrl())
                    .pbgDocumentUrl(permit.getPbgDocumentUrl())
                    .build();
        }

        return ProjectDetailResponse.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .type(project.getType().name())
                .status(project.getStatus().name())
                .locationDetail(project.getLocationDetail())
                .longitude(project.getLongitude())
                .latitude(project.getLatitude())
                .landArea(project.getLandArea())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .consultation(consultationInfo)
                .permit(permitInfo)
                .build();
    }

    private ProjectDetailResponse.ConsultationInfo buildConsultationInfo(Consultation consultation) {
        List<ConsultationHistory> history = consultationHistoryService.getConsultationHistory(consultation.getId());
        List<SurveySchedule> surveySchedules = surveyScheduleRepository.findByConsultationIdOrderByCreatedAtDesc(consultation.getId());

        // Get current survey schedule (latest approved or completed)
        SurveySchedule currentSchedule = surveySchedules.stream()
                .filter(s -> "APPROVED".equals(s.getStatus().name()) || "COMPLETED".equals(s.getStatus().name()))
                .findFirst()
                .orElse(null);

        // Get document versions
        List<DocumentVersion> documentVersions = documentVersionRepository.findByConsultationIdOrderByUploadedAtDesc(consultation.getId());

        // Build current document versions
        DocumentVersion currentDed = documentVersions.stream()
                .filter(d -> "DED".equals(d.getDocumentType()) && "FINAL".equals(d.getStatus()))
                .findFirst()
                .orElse(null);

        DocumentVersion currentBoq = documentVersions.stream()
                .filter(d -> "BOQ".equals(d.getDocumentType()) && "FINAL".equals(d.getStatus()))
                .findFirst()
                .orElse(null);

        DocumentVersion currentRab = documentVersions.stream()
                .filter(d -> "RAB".equals(d.getDocumentType()) && "FINAL".equals(d.getStatus()))
                .findFirst()
                .orElse(null);

        // Build document exchange history
        List<ProjectDetailResponse.DocumentExchange> exchangeHistory = documentVersions.stream()
                .map(this::mapToDocumentExchange)
                .collect(Collectors.toList());

        return ProjectDetailResponse.ConsultationInfo.builder()
                .consultationId(consultation.getId())
                .consultantName(consultation.getConsultant().getName())
                .scheduledAt(currentSchedule != null ? currentSchedule.getProposedDateTime().atZone(ZoneOffset.UTC).toLocalDateTime() : null)
                .status(consultation.getStatus().name())
                .notes("Konsultasi desain rumah minimalis dengan fokus efisiensi ruang")
                .consultationType(consultation.getConsultationType() != null ? consultation.getConsultationType().name() : null)
                .currentSurveySchedule(currentSchedule != null ? mapToSurveyScheduleInfo(currentSchedule) : null)
                .consultationHistory(history.stream()
                        .map(h -> mapToConsultationStep(h, surveySchedules))
                        .collect(Collectors.toList()))
                .documentExchange(ProjectDetailResponse.DocumentExchangeInfo.builder()
                        .currentDed(currentDed != null ? mapToDocumentVersion(currentDed) : null)
                        .currentBoq(currentBoq != null ? mapToDocumentVersion(currentBoq) : null)
                        .currentRab(currentRab != null ? mapToDocumentVersion(currentRab) : null)
                        .exchangeHistory(exchangeHistory)
                        .build())
                .revisionRequests(buildRevisionRequests(consultation.getId()))
                .surveySchedules(surveySchedules.stream()
                        .map(this::mapToSurveyScheduleInfo)
                        .collect(Collectors.toList()))
                .build();
    }

    private ProjectDetailResponse.ConsultationStep mapToConsultationStep(ConsultationHistory history, List<SurveySchedule> surveySchedules) {
        UUID contractId = history.getContractId();

        UUID scheduleId = history.getScheduleId();

        return ProjectDetailResponse.ConsultationStep.builder()
                .step(history.getStep())
                .state(history.getState())
                .title(history.getTitle())
                .subtitle(history.getSubtitle())
                .files(history.getFileIds() != null && !history.getFileIds().isEmpty()
                    ? history.getFileIds().stream()
                        .map(UUID::toString)  // Convert UUID to string for JSON response
                        .collect(Collectors.toList())
                    : history.getFiles())  // Fallback to old field if new field is empty
                .attachments(history.getAttachments())
                .metadata(ProjectDetailResponse.ConsultationMetadata.builder()
                        .dateTime(history.getDateTime())
                        .actor(history.getActor())
                        .status(history.getStatus())
                        .notes(history.getNotes())
                        .surveyCost(history.getSurveyCost())
                        .surveyLocation(history.getSurveyLocation())
                        .contractId(contractId)
                        .scheduleId(scheduleId)
                        .build())
                .build();
    }

    private ProjectDetailResponse.DocumentVersion mapToDocumentVersion(DocumentVersion doc) {
        return ProjectDetailResponse.DocumentVersion.builder()
                .documentType(doc.getDocumentType())
                .version(doc.getVersion())
                .fileUrl(doc.getFileUrl())
                .uploadedAt(doc.getUploadedAt())
                .uploadedBy(doc.getUploadedBy())
                .status(doc.getStatus())
                .build();
    }

    private ProjectDetailResponse.DocumentExchange mapToDocumentExchange(DocumentVersion doc) {
        return ProjectDetailResponse.DocumentExchange.builder()
                .documentType(doc.getDocumentType())
                .action("UPLOAD")
                .version(doc.getVersion())
                .fileUrl(doc.getFileUrl())
                .timestamp(doc.getUploadedAt())
                .actor(doc.getUploadedBy())
                .notes(doc.getNotes())
                .build();
    }

    private ProjectDetailResponse.SurveyScheduleInfo mapToSurveyScheduleInfo(SurveySchedule schedule) {
        return ProjectDetailResponse.SurveyScheduleInfo.builder()
                .scheduleId(schedule.getId())
                .proposedDateTime(schedule.getProposedDateTime() != null ? schedule.getProposedDateTime().atZone(ZoneOffset.UTC).toLocalDateTime() : null)
                .status(schedule.getStatus().name())
                .notes(schedule.getNotes())
                .proposedBy("CONSULTANT")
                .createdAt(schedule.getCreatedAt() != null ? schedule.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDateTime() : null)
                .updatedAt(schedule.getUpdatedAt() != null ? schedule.getUpdatedAt().atZone(ZoneOffset.UTC).toLocalDateTime() : null)
                .surveyCost(schedule.getSurveyCost())
                .surveyLocation(schedule.getSurveyLocation())
                .estimatedDurationHours(schedule.getEstimatedDurationHours())
                .surveyType(schedule.getSurveyType() != null ? schedule.getSurveyType() : "ON_SITE")
                .build();
    }

    private List<ProjectDetailResponse.RevisionRequest> buildRevisionRequests(UUID consultationId) {
        List<Revision> revisions = revisionRepository.findByConsultationIdOrderByCreatedAtDesc(consultationId);
        return revisions.stream()
                .map(this::mapToRevisionRequest)
                .collect(Collectors.toList());
    }

    private ProjectDetailResponse.RevisionRequest mapToRevisionRequest(Revision revision) {
        return ProjectDetailResponse.RevisionRequest.builder()
                .revisionId(revision.getId())
                .title("Revision " + revision.getVersion())
                .description(revision.getNotes())
                .priority("NORMAL") // Default priority
                .status(revision.getStatus().name())
                .requestedAt(revision.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDateTime())
                .completedAt(revision.getStatus() == RevisionStatus.APPROVED ?
                    revision.getUpdatedAt().atZone(ZoneOffset.UTC).toLocalDateTime() : null)
                .requestedBy("HOMEOWNER") // Default, could be enhanced
                .affectedDocuments(List.of()) // TODO: Add affected documents
                .resolution(revision.getStatus() == RevisionStatus.APPROVED ? "Revision completed successfully" : null)
                .build();
    }

}