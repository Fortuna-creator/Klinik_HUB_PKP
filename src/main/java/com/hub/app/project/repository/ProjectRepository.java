package com.hub.app.project.repository;

import com.hub.app.project.model.Project;
import com.hub.app.project.model.ProjectStatus;
import com.hub.app.project.model.ProjectType;
import com.hub.app.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByProjectId(UUID projectId);
    List<Project> findByUser(User user);
    Page<Project> findByUser(User user, Pageable pageable);
    Page<Project> findByUserAndType(User user, ProjectType type, Pageable pageable);
    Page<Project> findByUserAndStatus(User user, ProjectStatus status, Pageable pageable);
    List<Project> findByUserAndStatus(User user, ProjectStatus status);
    Page<Project> findByUserAndTypeAndStatus(User user, ProjectType type, ProjectStatus status, Pageable pageable);
    Page<Project> findByUserIdIn(List<Long> userIds, Pageable pageable);
    Page<Project> findByProjectIdIn(List<UUID> projectIds, Pageable pageable);
}