package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.CreateRevisionRequest;
import com.hub.app.consultation.dto.RevisionResponse;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.Revision;
import com.hub.app.consultation.model.RevisionStatus;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.consultation.repository.RevisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevisionService {

    private final RevisionRepository revisionRepository;
    private final ConsultationRepository consultationRepository;

    public RevisionResponse createRevision(UUID consultationId, CreateRevisionRequest request, Long userId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, userId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        // Check revision limit (max 2 free revisions)
        long revisionCount = revisionRepository.countByConsultationId(consultationId);
        if (revisionCount >= 2) {
            throw new RuntimeException("Maximum 2 revisions allowed for free consultations");
        }

        Revision revision = new Revision();
        revision.setConsultation(consultation);
        revision.setVersion("v" + (revisionCount + 1));
        revision.setNotes(request.getNotes());
        revision.setFileUrl(request.getFileUrl());
        revision.setStatus(RevisionStatus.PENDING);
        revision.setRevisionCount((int) revisionCount + 1);

        revision = revisionRepository.save(revision);
        return mapToResponse(revision);
    }

    public List<RevisionResponse> getRevisionsByConsultation(UUID consultationId, Long userId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        // Check if user has access to this consultation
        if (!consultation.getHomeOwner().getId().equals(userId) &&
            !consultation.getConsultant().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        List<Revision> revisions = revisionRepository.findByConsultationIdOrderByCreatedAtDesc(consultationId);
        return revisions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RevisionResponse mapToResponse(Revision revision) {
        return RevisionResponse.builder()
                .id(revision.getId())
                .consultationId(revision.getConsultation().getId())
                .version(revision.getVersion())
                .notes(revision.getNotes())
                .fileUrl(revision.getFileUrl())
                .status(revision.getStatus())
                .revisionCount(revision.getRevisionCount())
                .createdAt(revision.getCreatedAt())
                .updatedAt(revision.getUpdatedAt())
                .build();
    }
}