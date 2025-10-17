package com.hub.app.project.service;

import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.project.dto.*;
import com.hub.app.project.model.Project;
import com.hub.app.project.model.ProjectStatus;
import com.hub.app.project.model.ProjectType;
import com.hub.app.project.repository.ProjectRepository;
import com.hub.app.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;

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
        if (type != null && status != null) {
            projectsPage = projectRepository.findByUserAndTypeAndStatus(user, type, status, pageable);
        } else if (type != null) {
            projectsPage = projectRepository.findByUserAndType(user, type, pageable);
        } else if (status != null) {
            projectsPage = projectRepository.findByUserAndStatus(user, status, pageable);
        } else {
            projectsPage = projectRepository.findByUser(user, pageable);
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

        // Check if project belongs to user
        if (!project.getUser().getId().equals(user.getId())) {
            throw new DomainException(ErrorCode.UNAUTHORIZED);
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
                .consultation(null) // TODO: Add consultation integration
                .permit(null) // TODO: Add permit integration
                .build();
    }
}