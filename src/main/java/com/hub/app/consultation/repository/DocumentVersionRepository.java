package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    List<DocumentVersion> findByConsultationIdOrderByUploadedAtDesc(UUID consultationId);

    List<DocumentVersion> findByConsultationIdAndDocumentTypeOrderByVersionDesc(UUID consultationId, String documentType);

    @Query("SELECT dv FROM DocumentVersion dv WHERE dv.consultation.id = :consultationId AND dv.documentType = :documentType ORDER BY dv.version DESC")
    List<DocumentVersion> findLatestVersionsByConsultationAndType(
        @Param("consultationId") UUID consultationId,
        @Param("documentType") String documentType);

    @Query("SELECT dv FROM DocumentVersion dv WHERE dv.consultation.id = :consultationId AND dv.documentType = :documentType AND dv.status = 'FINAL' ORDER BY dv.uploadedAt DESC")
    Optional<DocumentVersion> findLatestFinalVersion(
        @Param("consultationId") UUID consultationId,
        @Param("documentType") String documentType);
}