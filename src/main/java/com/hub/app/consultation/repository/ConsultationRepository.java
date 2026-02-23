package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {

    List<Consultation> findByHomeOwnerId(Long homeOwnerId);

    List<Consultation> findByConsultantId(Long consultantId);

    @Query("SELECT c FROM Consultation c WHERE c.homeOwner.id = :homeOwnerId AND c.status = :status")
    List<Consultation> findByHomeOwnerIdAndStatus(@Param("homeOwnerId") Long homeOwnerId, @Param("status") ConsultationStatus status);

    @Query("SELECT c FROM Consultation c WHERE c.consultant.id = :consultantId AND c.status = :status")
    List<Consultation> findByConsultantIdAndStatus(@Param("consultantId") Long consultantId, @Param("status") ConsultationStatus status);

    Optional<Consultation> findByIdAndHomeOwnerId(UUID id, Long homeOwnerId);

    Optional<Consultation> findByIdAndConsultantId(UUID id, Long consultantId);

    Optional<Consultation> findByProjectId(UUID projectId);
}