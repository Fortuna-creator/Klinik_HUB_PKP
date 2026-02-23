package com.hub.app.project.service;

import com.hub.app.consultation.model.*;
import com.hub.app.consultation.repository.*;
import com.hub.app.consultation.service.ConsultationHistoryService;
import com.hub.app.permit.repository.PermitRepository;
import com.hub.app.project.dto.ProjectDetailResponse;
import com.hub.app.project.model.Project;
import com.hub.app.project.model.ProjectStatus;
import com.hub.app.project.model.ProjectType;
import com.hub.app.project.repository.ProjectRepository;
import com.hub.app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private ConsultationHistoryService consultationHistoryService;

    @Mock
    private SurveyScheduleRepository surveyScheduleRepository;

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private PermitRepository permitRepository;

    @Mock
    private RevisionRepository revisionRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private User user;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        user = new User();
        user.setId(1L);

        project = new Project();
        project.setProjectId(projectId);
        project.setName("Test Project");
        project.setType(ProjectType.PROTOTYPE);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setUser(user);
        project.setLongitude(new BigDecimal("106.8456"));
        project.setLatitude(new BigDecimal("-6.2088"));
        project.setLandArea(new BigDecimal("200.5"));
    }

    @Test
    void getProjectDetail_WithConsultation_ShouldReturnCompleteResponse() {
        // Given
        Consultation consultation = createConsultation();
        List<ConsultationHistory> history = Arrays.asList(createHistory("CONSULTATION", "CREATED"));
        List<SurveySchedule> surveySchedules = Arrays.asList(createSurveySchedule());
        List<DocumentVersion> documentVersions = Arrays.asList(createDocumentVersion("DED"));

        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.of(project));
        when(consultationRepository.findByHomeOwnerId(user.getId())).thenReturn(Arrays.asList(consultation));
        when(consultationHistoryService.getConsultationHistory(consultation.getId())).thenReturn(history);
        when(surveyScheduleRepository.findByConsultationIdOrderByCreatedAtDesc(consultation.getId())).thenReturn(surveySchedules);
        when(documentVersionRepository.findByConsultationIdOrderByUploadedAtDesc(consultation.getId())).thenReturn(documentVersions);
        when(permitRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(revisionRepository.findByConsultationIdOrderByCreatedAtDesc(consultation.getId())).thenReturn(Arrays.asList());

        // When
        ProjectDetailResponse response = projectService.getProjectDetail(projectId, user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isEqualTo(projectId);
        assertThat(response.getName()).isEqualTo("Test Project");
        assertThat(response.getConsultation()).isNotNull();
        assertThat(response.getConsultation().getConsultationId()).isEqualTo(consultation.getId());
        assertThat(response.getConsultation().getConsultationHistory()).hasSize(1);
        assertThat(response.getConsultation().getSurveySchedules()).hasSize(1);
        assertThat(response.getConsultation().getDocumentExchange()).isNotNull();
    }

    @Test
    void getProjectDetail_WithoutConsultation_ShouldReturnResponseWithNullConsultation() {
        // Given
        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.of(project));
        when(consultationRepository.findByHomeOwnerId(user.getId())).thenReturn(Arrays.asList());
        when(permitRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

        // When
        ProjectDetailResponse response = projectService.getProjectDetail(projectId, user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getConsultation()).isNull();
    }

    @Test
    void getProjectDetail_WithCurrentSurveySchedule_ShouldIncludeCurrentSchedule() {
        // Given
        Consultation consultation = createConsultation();
        SurveySchedule approvedSchedule = createSurveySchedule();
        approvedSchedule.setStatus(SurveyStatus.APPROVED);

        SurveySchedule rejectedSchedule = createSurveySchedule();
        rejectedSchedule.setStatus(SurveyStatus.REJECTED);

        List<SurveySchedule> schedules = Arrays.asList(rejectedSchedule, approvedSchedule);

        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.of(project));
        when(consultationRepository.findByHomeOwnerId(user.getId())).thenReturn(Arrays.asList(consultation));
        when(consultationHistoryService.getConsultationHistory(consultation.getId())).thenReturn(Arrays.asList());
        when(surveyScheduleRepository.findByConsultationIdOrderByCreatedAtDesc(consultation.getId())).thenReturn(schedules);
        when(documentVersionRepository.findByConsultationIdOrderByUploadedAtDesc(consultation.getId())).thenReturn(Arrays.asList());
        when(permitRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(revisionRepository.findByConsultationIdOrderByCreatedAtDesc(consultation.getId())).thenReturn(Arrays.asList());

        // When
        ProjectDetailResponse response = projectService.getProjectDetail(projectId, user);

        // Then
        assertThat(response.getConsultation().getCurrentSurveySchedule()).isNotNull();
        assertThat(response.getConsultation().getCurrentSurveySchedule().getStatus()).isEqualTo("APPROVED");
    }

    private Consultation createConsultation() {
        Consultation consultation = new Consultation();
        consultation.setId(UUID.randomUUID());
        consultation.setStatus(ConsultationStatus.AKTIF);
        // Set up HomeOwner to avoid NPE
        com.hub.app.user.model.HomeOwner homeOwner = new com.hub.app.user.model.HomeOwner();
        homeOwner.setId(user.getId());
        consultation.setHomeOwner(homeOwner);
        // Set up Consultant to avoid NPE
        com.hub.app.user.model.Consultant consultant = new com.hub.app.user.model.Consultant();
        consultant.setName("Test Consultant");
        consultation.setConsultant(consultant);
        return consultation;
    }

    private ConsultationHistory createHistory(String step, String state) {
        ConsultationHistory history = new ConsultationHistory();
        history.setId(UUID.randomUUID());
        history.setStep(step);
        history.setState(state);
        history.setTitle(step + " " + state);
        history.setDateTime(LocalDateTime.now());
        return history;
    }

    private SurveySchedule createSurveySchedule() {
        SurveySchedule schedule = new SurveySchedule();
        schedule.setId(UUID.randomUUID());
        schedule.setProposedDateTime(java.time.Instant.now().plusSeconds(86400)); // 1 day from now
        schedule.setStatus(SurveyStatus.PENDING);
        schedule.setSurveyCost(new BigDecimal("1500000.00"));
        schedule.setSurveyLocation("Jakarta Selatan");
        schedule.setEstimatedDurationHours(3);
        schedule.setSurveyType("ON_SITE");
        return schedule;
    }

    private DocumentVersion createDocumentVersion(String type) {
        DocumentVersion doc = new DocumentVersion();
        doc.setId(UUID.randomUUID());
        doc.setDocumentType(type);
        doc.setVersion("1.0");
        doc.setFileUrl("https://example.com/" + type.toLowerCase() + "_v1.0.pdf");
        doc.setStatus("FINAL");
        doc.setUploadedAt(LocalDateTime.now());
        return doc;
    }
}