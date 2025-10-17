package com.hub.app.project.service;

import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.project.dto.CreateProjectRequest;
import com.hub.app.project.dto.CreateProjectResponse;
import com.hub.app.project.model.Project;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private CreateProjectRequest request;
    private Project savedProject;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        request = new CreateProjectRequest();
        request.setName("Test Project");
        request.setLongitude(new BigDecimal("106.84513"));
        request.setLatitude(new BigDecimal("-6.21462"));
        request.setLocationDetail("Jl. Sudirman No.1");
        request.setType(ProjectType.PROTOTYPE);
        request.setLandArea(new BigDecimal("120.5"));
        request.setIncome(new BigDecimal("50000000"));

        savedProject = Project.builder()
                .id(1L)
                .projectId(UUID.randomUUID())
                .name("Test Project")
                .longitude(new BigDecimal("106.84513"))
                .latitude(new BigDecimal("-6.21462"))
                .locationDetail("Jl. Sudirman No.1")
                .type(ProjectType.PROTOTYPE)
                .landArea(new BigDecimal("120.5"))
                .income(new BigDecimal("50000000"))
                .user(testUser)
                .build();
    }

    @Test
    void createProject_ValidRequest_ReturnsResponse() {
        // Arrange
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        // Act
        CreateProjectResponse response = projectService.createProject(request, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(savedProject.getProjectId(), response.getProjectId());
        assertEquals(savedProject.getName(), response.getName());
        assertEquals(savedProject.getType().name(), response.getType());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void getProjectDetail_ExistingProject_ReturnsDetail() {
        // Arrange
        UUID projectId = savedProject.getProjectId();
        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.of(savedProject));

        // Act
        var response = projectService.getProjectDetail(projectId, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(projectId, response.getProjectId());
        assertEquals("Test Project", response.getName());
        assertEquals("PROTOTYPE", response.getType());
    }

    @Test
    void getProjectDetail_NonExistingProject_ThrowsException() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class,
                () -> projectService.getProjectDetail(projectId, testUser));
        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getProjectDetail_WrongUser_ThrowsException() {
        // Arrange
        UUID projectId = savedProject.getProjectId();
        User differentUser = new User();
        differentUser.setId(2L);
        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.of(savedProject));

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class,
                () -> projectService.getProjectDetail(projectId, differentUser));
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }
}