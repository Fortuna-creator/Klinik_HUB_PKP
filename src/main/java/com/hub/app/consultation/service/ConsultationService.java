package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.ConsultationResponse;
import com.hub.app.consultation.dto.CreateConsultationRequest;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.HomeOwner;
import com.hub.app.user.repository.ConsultantRepository;
import com.hub.app.user.repository.HomeOwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ConsultationService {

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private HomeOwnerRepository homeOwnerRepository;

    @Autowired
    private ConsultantRepository consultantRepository;

    public ConsultationResponse acceptConsultation(UUID consultationId, Long consultantId) {
        Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        if (consultation.getStatus() != ConsultationStatus.MENUNGGU_KONFIRMASI_KONSULTAN) {
            throw new RuntimeException("Invalid status transition");
        }

        consultation.setStatus(ConsultationStatus.MENUNGGU_JADWAL_SURVEY);
        consultation = consultationRepository.save(consultation);
        return mapToResponse(consultation);
    }

    public ConsultationResponse rejectConsultation(UUID consultationId, Long consultantId) {
        Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        if (consultation.getStatus() != ConsultationStatus.MENUNGGU_KONFIRMASI_KONSULTAN) {
            throw new RuntimeException("Invalid status transition");
        }

        consultation.setStatus(ConsultationStatus.SELESAI); // Consultation rejected
        consultation = consultationRepository.save(consultation);
        return mapToResponse(consultation);
    }

    public ConsultationResponse createConsultation(Long homeOwnerId, CreateConsultationRequest request) {
        HomeOwner homeOwner = homeOwnerRepository.findById(homeOwnerId)
                .orElseThrow(() -> new RuntimeException("Home Owner not found"));

        Consultant consultant = consultantRepository.findById(request.getConsultantId())
                .orElseThrow(() -> new RuntimeException("Consultant not found"));

        Consultation consultation = new Consultation();
        consultation.setHomeOwner(homeOwner);
        consultation.setConsultant(consultant);
        consultation.setConsultationType(request.getConsultationType());
        consultation.setChannel(request.getChannel());
        consultation.setStatus(ConsultationStatus.MENUNGGU_KONFIRMASI_KONSULTAN);

        consultation = consultationRepository.save(consultation);
        return mapToResponse(consultation);
    }

    public List<ConsultationResponse> getConsultationsByHomeOwner(Long homeOwnerId) {
        List<Consultation> consultations = consultationRepository.findByHomeOwnerId(homeOwnerId);
        return consultations.stream().map(this::mapToResponse).toList();
    }

    public List<ConsultationResponse> getConsultationsByConsultant(Long consultantId) {
        List<Consultation> consultations = consultationRepository.findByConsultantId(consultantId);
        return consultations.stream().map(this::mapToResponse).toList();
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

        if (consultation.getStatus() != ConsultationStatus.FINAL) {
            throw new RuntimeException("Consultation is not in final status");
        }

        consultation.setStatus(ConsultationStatus.SELESAI);
        consultation.setCompletedAt(Instant.now());
        consultation = consultationRepository.save(consultation);
        return mapToResponse(consultation);
    }

    public ConsultationResponse mapToResponse(Consultation consultation) {
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