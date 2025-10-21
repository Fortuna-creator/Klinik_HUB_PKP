package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.FinalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinalDocumentRepository extends JpaRepository<FinalDocument, UUID> {

    Optional<FinalDocument> findByConsultationId(UUID consultationId);
}