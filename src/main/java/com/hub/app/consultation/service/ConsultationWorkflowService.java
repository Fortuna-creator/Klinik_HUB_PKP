package com.hub.app.consultation.service;

import com.hub.app.consultation.event.ConsultationEvent;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultationWorkflowService {

    private final ConsultationRepository consultationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ConsultationHistoryService historyService;

    /**
     * Transisi dari SURVEY_SELESAI ke AKTIF
     * Dipanggil setelah survey selesai dan kontrak sudah ditandatangani
     */
    @Transactional
    public void startActiveConsultation(UUID consultationId, Long userId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, userId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (consultation.getStatus() != ConsultationStatus.SURVEY_SELESAI) {
            throw new DomainException(ErrorCode.INVALID_STATUS);
        }

        // Validasi bahwa kontrak sudah ditandatangani
        // (asumsi ada method untuk cek ini, atau bisa ditambahkan validasi di sini)

        consultation.setStatus(ConsultationStatus.AKTIF);
        consultation.setStartedAt(Instant.now());
        consultation = consultationRepository.save(consultation);

        // Publish event
        eventPublisher.publishEvent(new ConsultationEvent(
            this, consultation, "CONSULTATION_STARTED", "CONSULTATION", "STARTED", "SYSTEM"
        ));

        // Create history event
        historyService.createHistoryEvent(
            consultation,
            "CONSULTATION",
            "STARTED",
            "Konsultasi aktif dimulai",
            "Konsultasi desain telah dimulai setelah survey selesai",
            java.util.Arrays.asList(),
            java.util.Arrays.asList(),
            "SYSTEM",
            "STARTED",
            "Konsultasi aktif"
        );
    }

    /**
     * Transisi dari AKTIF ke REVISI
     * Dipanggil ketika homeowner meminta revisi
     */
    @Transactional
    public void startRevision(UUID consultationId, Long homeOwnerId, String revisionNotes) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, homeOwnerId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (consultation.getStatus() != ConsultationStatus.AKTIF) {
            throw new DomainException(ErrorCode.INVALID_STATUS);
        }

        consultation.setStatus(ConsultationStatus.REVISI);
        consultation = consultationRepository.save(consultation);

        // Publish event
        eventPublisher.publishEvent(new ConsultationEvent(
            this, consultation, "REVISION_STARTED", "REVISION", "STARTED", "HOMEOWNER"
        ));

        // Create history event
        historyService.createHistoryEvent(
            consultation,
            "REVISION",
            "STARTED",
            "Revisi dimulai",
            revisionNotes != null ? revisionNotes : "Homeowner meminta revisi desain",
            java.util.Arrays.asList(),
            java.util.Arrays.asList(),
            "HOMEOWNER",
            "STARTED",
            "Revisi dimulai"
        );
    }

    /**
     * Transisi dari REVISI ke FINAL
     * Dipanggil ketika revisi selesai dan homeowner menyetujui
     */
    @Transactional
    public void finalizeConsultation(UUID consultationId, Long homeOwnerId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, homeOwnerId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (consultation.getStatus() != ConsultationStatus.REVISI) {
            throw new DomainException(ErrorCode.INVALID_STATUS);
        }

        consultation.setStatus(ConsultationStatus.FINAL);
        consultation = consultationRepository.save(consultation);

        // Publish event
        eventPublisher.publishEvent(new ConsultationEvent(
            this, consultation, "CONSULTATION_FINALIZED", "CONSULTATION", "FINALIZED", "HOMEOWNER"
        ));

        // Create history event
        historyService.createHistoryEvent(
            consultation,
            "CONSULTATION",
            "FINALIZED",
            "Konsultasi difinalisasi",
            "Homeowner menyetujui hasil akhir konsultasi",
            java.util.Arrays.asList(),
            java.util.Arrays.asList(),
            "HOMEOWNER",
            "FINALIZED",
            "Konsultasi selesai"
        );
    }

    /**
     * Validasi transisi status
     */
    public boolean isValidTransition(ConsultationStatus currentStatus, ConsultationStatus newStatus) {
        switch (currentStatus) {
            case MENUNGGU_KONFIRMASI_KONSULTAN:
                return newStatus == ConsultationStatus.MENUNGGU_JADWAL_SURVEY ||
                       newStatus == ConsultationStatus.SELESAI;
            case MENUNGGU_JADWAL_SURVEY:
                return newStatus == ConsultationStatus.MENUNGGU_APPROVAL_JADWAL;
            case MENUNGGU_APPROVAL_JADWAL:
                return newStatus == ConsultationStatus.SURVEY_DIJADWALKAN ||
                       newStatus == ConsultationStatus.MENUNGGU_JADWAL_SURVEY;
            case SURVEY_DIJADWALKAN:
                return newStatus == ConsultationStatus.SURVEY_SELESAI;
            case SURVEY_SELESAI:
                return newStatus == ConsultationStatus.AKTIF;
            case AKTIF:
                return newStatus == ConsultationStatus.REVISI ||
                       newStatus == ConsultationStatus.FINAL;
            case REVISI:
                return newStatus == ConsultationStatus.FINAL ||
                       newStatus == ConsultationStatus.AKTIF; // Kembali ke aktif jika revisi ditolak
            case FINAL:
                return newStatus == ConsultationStatus.SELESAI;
            case SELESAI:
                return false; // Status final, tidak bisa berubah
            default:
                return false;
        }
    }

    /**
     * Get next possible statuses for current status
     */
    public ConsultationStatus[] getNextStatuses(ConsultationStatus currentStatus) {
        switch (currentStatus) {
            case MENUNGGU_KONFIRMASI_KONSULTAN:
                return new ConsultationStatus[]{ConsultationStatus.MENUNGGU_JADWAL_SURVEY, ConsultationStatus.SELESAI};
            case MENUNGGU_JADWAL_SURVEY:
                return new ConsultationStatus[]{ConsultationStatus.MENUNGGU_APPROVAL_JADWAL};
            case MENUNGGU_APPROVAL_JADWAL:
                return new ConsultationStatus[]{ConsultationStatus.SURVEY_DIJADWALKAN, ConsultationStatus.MENUNGGU_JADWAL_SURVEY};
            case SURVEY_DIJADWALKAN:
                return new ConsultationStatus[]{ConsultationStatus.SURVEY_SELESAI};
            case SURVEY_SELESAI:
                return new ConsultationStatus[]{ConsultationStatus.AKTIF};
            case AKTIF:
                return new ConsultationStatus[]{ConsultationStatus.REVISI, ConsultationStatus.FINAL};
            case REVISI:
                return new ConsultationStatus[]{ConsultationStatus.FINAL, ConsultationStatus.AKTIF};
            case FINAL:
                return new ConsultationStatus[]{ConsultationStatus.SELESAI};
            case SELESAI:
                return new ConsultationStatus[]{};
            default:
                return new ConsultationStatus[]{};
        }
    }
}