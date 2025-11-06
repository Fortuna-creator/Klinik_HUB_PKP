package com.hub.app.consultationoffline.service;

import com.hub.app.balaioffline.model.Balai;
import com.hub.app.balaioffline.repository.BalaiRepository;
import com.hub.app.consultationoffline.dto.*;
import com.hub.app.consultationoffline.model.ConsultationStatus;
import com.hub.app.consultationoffline.model.OfflineConsultation;
import com.hub.app.consultationoffline.repository.OfflineConsultationRepository;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfflineConsultationService {

    private static final Logger logger = LoggerFactory.getLogger(OfflineConsultationService.class);

    private final OfflineConsultationRepository consultationRepository;
    private final UserRepository userRepository;
    private final BalaiRepository balaiRepository;

    private static final LocalTime BUSINESS_HOURS_START = LocalTime.of(8, 0);
    private static final LocalTime BUSINESS_HOURS_END = LocalTime.of(17, 0);

    @Transactional
    public OfflineConsultationResponse createConsultation(CreateOfflineConsultationRequest request) {
        logger.info("Creating consultation for user {} at balai {}", request.getUserId(), request.getBalaiId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        // Validate balai exists
        UUID balaiId = UUID.fromString(request.getBalaiId());
        Balai balai = balaiRepository.findById(balaiId)
                .orElseThrow(() -> new DomainException(ErrorCode.BALAI_NOT_FOUND));

        // Validate date is not in the past
        if (request.getConsultationDate().isBefore(LocalDate.now())) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_DATE);
        }

        // Validate time is within business hours
        if (request.getConsultationTime().isBefore(BUSINESS_HOURS_START) ||
            request.getConsultationTime().isAfter(BUSINESS_HOURS_END)) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_TIME);
        }

        // Check for duplicate booking
        boolean exists = consultationRepository.existsByUserAndBalaiAndDateAndTime(
                request.getUserId(),
                balaiId,
                request.getConsultationDate(),
                request.getConsultationTime()
        );

        if (exists) {
            throw new DomainException(ErrorCode.CONSULTATION_ALREADY_BOOKED);
        }

        // Create consultation
        OfflineConsultation consultation = OfflineConsultation.builder()
                .user(user)
                .balai(balai)
                .consultationDate(request.getConsultationDate())
                .consultationTime(request.getConsultationTime())
                .status(ConsultationStatus.PENDING)
                .build();

        consultation = consultationRepository.save(consultation);
        logger.info("Successfully created consultation with id: {}", consultation.getId());

        return mapToResponse(consultation);
    }

    @Transactional(readOnly = true)
    public List<OfflineConsultationResponse> getAllConsultations() {
        logger.info("Fetching all consultations");
        List<OfflineConsultation> consultations = consultationRepository.findAll();
        return consultations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OfflineConsultationResponse getConsultationById(UUID id) {
        logger.info("Fetching consultation by id: {}", id);
        OfflineConsultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.OFFLINE_CONSULTATION_NOT_FOUND));
        return mapToResponse(consultation);
    }

    @Transactional
    public OfflineConsultationResponse updateConsultationStatus(UUID id, UpdateConsultationStatusRequest request) {
        logger.info("Updating consultation {} status to {}", id, request.getStatus());

        OfflineConsultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.OFFLINE_CONSULTATION_NOT_FOUND));

        ConsultationStatus newStatus;
        try {
            newStatus = ConsultationStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_STATUS, "Invalid status value");
        }

        // Validate status transition
        validateStatusTransition(consultation.getStatus(), newStatus);

        consultation.setStatus(newStatus);
        consultation = consultationRepository.save(consultation);

        logger.info("Successfully updated consultation status");
        return mapToResponse(consultation);
    }

    @Transactional
    public void deleteConsultation(UUID id) {
        logger.info("Deleting consultation: {}", id);

        if (!consultationRepository.existsById(id)) {
            throw new DomainException(ErrorCode.OFFLINE_CONSULTATION_NOT_FOUND);
        }

        consultationRepository.deleteById(id);
        logger.info("Successfully deleted consultation");
    }

    private void validateStatusTransition(ConsultationStatus currentStatus, ConsultationStatus newStatus) {
        // Cannot change status of cancelled consultation
        if (currentStatus == ConsultationStatus.CANCELLED) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Cannot change status of a cancelled consultation");
        }

        // Cannot change status of completed consultation
        if (currentStatus == ConsultationStatus.COMPLETED) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Cannot change status of a completed consultation");
        }

        // Pending can go to: confirmed, cancelled
        if (currentStatus == ConsultationStatus.PENDING &&
            newStatus != ConsultationStatus.CONFIRMED &&
            newStatus != ConsultationStatus.CANCELLED) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Pending consultation can only be confirmed or cancelled");
        }

        // Confirmed can go to: completed, cancelled
        if (currentStatus == ConsultationStatus.CONFIRMED &&
            newStatus != ConsultationStatus.COMPLETED &&
            newStatus != ConsultationStatus.CANCELLED) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Confirmed consultation can only be completed or cancelled");
        }
    }

    private OfflineConsultationResponse mapToResponse(OfflineConsultation consultation) {
        return OfflineConsultationResponse.builder()
                .id(consultation.getId())
                .user(UserSummary.builder()
                        .id(consultation.getUser().getId())
                        .name(consultation.getUser().getName())
                        .build())
                .balai(BalaiSummary.builder()
                        .id(consultation.getBalai().getId())
                        .name(consultation.getBalai().getName())
                        .build())
                .consultationDate(consultation.getConsultationDate())
                .consultationTime(consultation.getConsultationTime())
                .status(consultation.getStatus().name().toLowerCase())
                .createdAt(consultation.getCreatedAt())
                .updatedAt(consultation.getUpdatedAt())
                .build();
    }
}

