package com.hub.app.consultation.service;

import com.hub.app.consultation.event.ConsultationEvent;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.DocumentVersion;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.consultation.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentVersioningService {

    private final DocumentVersionRepository documentVersionRepository;
    private final ConsultationRepository consultationRepository;
    private final ConsultationHistoryService historyService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DocumentVersion uploadDocumentVersion(UUID consultationId, Long consultantId,
                                               String documentType, String version,
                                               String fileUrl, String notes) {

        Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setConsultation(consultation);
        documentVersion.setDocumentType(documentType);
        documentVersion.setVersion(version);
        documentVersion.setFileUrl(fileUrl);
        documentVersion.setUploadedBy("CONSULTANT");
        documentVersion.setStatus("UPLOADED");
        documentVersion.setNotes(notes);

        documentVersion = documentVersionRepository.save(documentVersion);

        // Publish event for history tracking
        eventPublisher.publishEvent(new ConsultationEvent(
            this, consultation, "DOCUMENT_" + documentType + "_UPLOADED", "DOCUMENT",
            documentType.toLowerCase() + "_uploaded", "CONSULTANT",
            java.util.Arrays.asList(), java.util.Arrays.asList(), null, null  // No fileIds available
        ));

        return documentVersion;
    }

    @Transactional
    public DocumentVersion markDocumentAsFinal(UUID documentVersionId, Long consultantId) {

        DocumentVersion documentVersion = documentVersionRepository.findById(documentVersionId)
                .orElseThrow(() -> new RuntimeException("Document version not found"));

        if (!documentVersion.getConsultation().getConsultant().getId().equals(consultantId)) {
            throw new RuntimeException("Access denied");
        }

        documentVersion.setStatus("FINAL");
        documentVersion = documentVersionRepository.save(documentVersion);

        // Publish event for history tracking
        eventPublisher.publishEvent(new ConsultationEvent(
            this, documentVersion.getConsultation(), "DOCUMENT_" + documentVersion.getDocumentType() + "_FINAL",
            "DOCUMENT", documentVersion.getDocumentType().toLowerCase() + "_final", "CONSULTANT",
            java.util.Arrays.asList(), java.util.Arrays.asList(), null, null  // No fileIds available
        ));

        return documentVersion;
    }

    public List<DocumentVersion> getDocumentVersions(UUID consultationId, String documentType) {
        if (documentType != null) {
            return documentVersionRepository.findByConsultationIdAndDocumentTypeOrderByVersionDesc(consultationId, documentType);
        } else {
            return documentVersionRepository.findByConsultationIdOrderByUploadedAtDesc(consultationId);
        }
    }

    public Optional<DocumentVersion> getLatestFinalVersion(UUID consultationId, String documentType) {
        return documentVersionRepository.findLatestFinalVersion(consultationId, documentType);
    }

    public List<DocumentVersion> getLatestVersionsByType(UUID consultationId, String documentType) {
        return documentVersionRepository.findLatestVersionsByConsultationAndType(consultationId, documentType);
    }

    private String getDocumentTypeName(String documentType) {
        switch (documentType) {
            case "DED": return "Detail Engineering Design";
            case "BOQ": return "Bill of Quantities";
            case "RAB": return "Rencana Anggaran Biaya";
            default: return documentType;
        }
    }
}