package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.ConsultationResponse;
import com.hub.app.consultation.dto.CreateConsultationRequest;
import com.hub.app.consultation.model.*;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.HomeOwner;
import com.hub.app.user.repository.ConsultantRepository;
import com.hub.app.user.repository.HomeOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final HomeOwnerRepository homeOwnerRepository;
    private final ConsultantRepository consultantRepository;
    private final DesignService designService;

    public ConsultationResponse createConsultation(Long homeOwnerId, CreateConsultationRequest request) {
        HomeOwner homeOwner = homeOwnerRepository.findById(homeOwnerId)
                .orElseThrow(() -> new RuntimeException("HomeOwner not found"));

        Consultant consultant = consultantRepository.findById(request.getConsultantId())
                .orElseThrow(() -> new RuntimeException("Consultant not found"));

        // Note: Design is not required for consultation creation
        // Design will be created/uploaded by consultant after consultation starts

        Consultation consultation = new Consultation();
        consultation.setHomeOwner(homeOwner);
        consultation.setConsultant(consultant);
        consultation.setDesign(null); // Design will be assigned later by consultant
        consultation.setConsultationType(request.getConsultationType());
        consultation.setChannel(request.getChannel());
        consultation.setStatus(ConsultationStatus.DRAFT);

        consultation = consultationRepository.save(consultation);
        return mapToResponse(consultation);
    }

    public List<ConsultationResponse> getConsultationsByHomeOwner(Long homeOwnerId) {
        List<Consultation> consultations = consultationRepository.findByHomeOwnerId(homeOwnerId);
        return consultations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ConsultationResponse> getConsultationsByConsultant(Long consultantId) {
        List<Consultation> consultations = consultationRepository.findByConsultantId(consultantId);
        return consultations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ConsultationResponse getConsultationById(UUID id, Long userId, boolean isHomeOwner) {
        Consultation consultation;
        if (isHomeOwner) {
            consultation = consultationRepository.findByIdAndHomeOwnerId(id, userId)
                    .orElseThrow(() -> new RuntimeException("Consultation not found"));
        } else {
            consultation = consultationRepository.findByIdAndConsultantId(id, userId)
                    .orElseThrow(() -> new RuntimeException("Consultation not found"));
        }
        return mapToResponse(consultation);
    }

    public ConsultationResponse finalizeConsultation(UUID id, Long homeOwnerId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(id, homeOwnerId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        consultation.setStatus(ConsultationStatus.FINAL);
        consultation.setCompletedAt(java.time.Instant.now());
        consultation = consultationRepository.save(consultation);
        return mapToResponse(consultation);
    }

    private ConsultationResponse mapToResponse(Consultation consultation) {
        return ConsultationResponse.builder()
                .id(consultation.getId())
                .homeOwnerId(consultation.getHomeOwner().getId())
                .homeOwnerName(consultation.getHomeOwner().getName())
                .consultantId(consultation.getConsultant().getId())
                .consultantName(consultation.getConsultant().getName())
                .designId(consultation.getDesign() != null ? consultation.getDesign().getId() : null)
                .designName(consultation.getDesign() != null ? consultation.getDesign().getName() : null)
                .consultationType(consultation.getConsultationType())
                .channel(consultation.getChannel())
                .status(consultation.getStatus())
                .startedAt(consultation.getStartedAt())
                .completedAt(consultation.getCompletedAt())
                .createdAt(consultation.getCreatedAt())
                .updatedAt(consultation.getUpdatedAt())
                .build();
    }
}