package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.FinalDocumentResponse;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.FinalDocument;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.consultation.repository.FinalDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinalDocumentService {

    private final FinalDocumentRepository finalDocumentRepository;
    private final ConsultationRepository consultationRepository;

    public FinalDocumentResponse uploadFinalDocument(UUID consultationId, String fileDedUrl, String fileRabUrl, String fileSpektekUrl, Long consultantId) {
        Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        FinalDocument finalDocument = new FinalDocument();
        finalDocument.setConsultation(consultation);
        finalDocument.setFileDedUrl(fileDedUrl);
        finalDocument.setFileRabUrl(fileRabUrl);
        finalDocument.setFileSpektekUrl(fileSpektekUrl);
        finalDocument.setStatus(com.hub.app.consultation.model.FinalDocumentStatus.FINAL);

        finalDocument = finalDocumentRepository.save(finalDocument);
        return mapToResponse(finalDocument);
    }

    public FinalDocumentResponse getFinalDocument(UUID consultationId, Long userId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        // Check if user has access
        if (!consultation.getHomeOwner().getId().equals(userId) &&
            !consultation.getConsultant().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        FinalDocument finalDocument = finalDocumentRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.FINAL_DOCUMENT_NOT_FOUND));

        return mapToResponse(finalDocument);
    }

    public FinalDocumentResponse archiveFinalDocument(UUID id, Long consultantId) {
        FinalDocument finalDocument = finalDocumentRepository.findById(id)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.FINAL_DOCUMENT_NOT_FOUND));

        // Check if consultant owns this document
        if (!finalDocument.getConsultation().getConsultant().getId().equals(consultantId)) {
            throw new RuntimeException("Access denied");
        }

        // Archive logic - could set status or delete
        finalDocumentRepository.delete(finalDocument);
        return mapToResponse(finalDocument);
    }

    private FinalDocumentResponse mapToResponse(FinalDocument finalDocument) {
        return FinalDocumentResponse.builder()
                .id(finalDocument.getId())
                .consultationId(finalDocument.getConsultation().getId())
                .fileDedUrl(finalDocument.getFileDedUrl())
                .fileRabUrl(finalDocument.getFileRabUrl())
                .fileSpektekUrl(finalDocument.getFileSpektekUrl())
                .status(finalDocument.getStatus())
                .createdAt(finalDocument.getCreatedAt())
                .updatedAt(finalDocument.getUpdatedAt())
                .build();
    }
}