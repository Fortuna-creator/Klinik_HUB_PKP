package com.hub.app.permit.repository;

import com.hub.app.permit.model.Permit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermitRepository extends JpaRepository<Permit, UUID> {

    Optional<Permit> findByProjectId(UUID projectId);
}