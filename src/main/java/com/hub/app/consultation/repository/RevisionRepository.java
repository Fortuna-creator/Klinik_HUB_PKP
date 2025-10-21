package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RevisionRepository extends JpaRepository<Revision, UUID> {

    List<Revision> findByConsultationIdOrderByCreatedAtDesc(UUID consultationId);

    @Query("SELECT COUNT(r) FROM Revision r WHERE r.consultation.id = :consultationId")
    long countByConsultationId(@Param("consultationId") UUID consultationId);
}