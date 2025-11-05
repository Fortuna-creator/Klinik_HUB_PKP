package com.hub.app.consultation.event;

import com.hub.app.consultation.service.ConsultationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultationEventListener {

    private final ConsultationHistoryService historyService;

    @EventListener
    @Async
    public void handleConsultationEvent(ConsultationEvent event) {
        log.info("=== EVENT LISTENER TRIGGERED ===");
        log.info("Event Type: {}, Step: {}, State: {}, Actor: {}", event.getEventType(), event.getStep(), event.getState(), event.getActor());
        log.info("Consultation ID: {}", event.getConsultation().getId());

        try {
            log.info("Processing consultation event: {} - {} - {}",
                    event.getEventType(), event.getStep(), event.getState());

            String title = generateTitle(event);
            String subtitle = generateSubtitle(event);
            String notes = generateNotes(event);

            log.debug("Generated content - Title: {}, Subtitle: {}, Notes: {}", title, subtitle, notes);

            log.info("=== CALLING HISTORY SERVICE ===");
            historyService.createHistoryEvent(
                event.getConsultation(),
                event.getStep(),
                event.getState(),
                title,
                subtitle,
                event.getFiles(),
                event.getAttachments(),
                event.getActor(),
                event.getState(),
                notes,
                event.getSurveyCost(),
                event.getSurveyLocation(),
                event.getScheduleId(),
                event.getContractId()
            );

            log.info("=== EVENT PROCESSING COMPLETED SUCCESSFULLY ===");

        } catch (Exception e) {
            log.error("=== EVENT PROCESSING FAILED ===");
            log.error("Event Type: {}, Step: {}, State: {}, Error: {}", event.getEventType(), event.getStep(), event.getState(), e.getMessage(), e);
        }
    }

    private String generateTitle(ConsultationEvent event) {
        switch (event.getStep() + "_" + event.getState()) {
            case "CONSULTATION_CREATED":
                return "Permintaan konsultasi dibuat";
            case "CONSULTATION_ACCEPTED":
                return "Konsultasi diterima konsultan";
            case "SURVEY_SCHEDULED":
                return "Konsultan mengajukan jadwal survey";
            case "SURVEY_APPROVED":
                return "Jadwal survey disetujui";
            case "SURVEY_REJECTED":
                return "Jadwal survey ditolak homeowner";
            case "SURVEY_RESCHEDULED":
                return "Homeowner mengajukan jadwal survey baru";
            case "SURVEY_COMPLETED":
                return "Survey lokasi selesai";
            case "CONTRACT_DRAFT_CREATED":
                return "Draft kontrak dibuat konsultan";
            case "CONTRACT_REQUEST_FOR_APPROVAL":
                return "Draft kontrak diajukan untuk approval";
            case "CONTRACT_APPROVED":
                return "Kontrak disetujui homeowner";
            case "CONTRACT_REJECTED":
                return "Kontrak ditolak homeowner";
            case "CONTRACT_REVISION_REQUESTED":
                return "Permintaan revisi kontrak";
            case "PAYMENT_REQUESTED":
                return "Konsultan meminta pembayaran";
            case "PAYMENT_APPROVED":
                return "Homeowner menyetujui pembayaran";
            case "DOCUMENT_DOCUMENTS_UPLOADED":
                return "Dokumen DED, BOQ, dan RAB diupload";
            case "DOCUMENT_REVISION_REQUESTED":
                return "Homeowner meminta revisi dokumen";
            case "DOCUMENT_DOCUMENTS_REVISED":
                return "Dokumen direvisi berdasarkan permintaan";
            case "DOCUMENT_DOCUMENT_FINAL":
                return "Semua dokumen final disetujui";
            default:
                return event.getStep() + " - " + event.getState();
        }
    }

    private String generateSubtitle(ConsultationEvent event) {
        switch (event.getStep() + "_" + event.getState()) {
            case "CONSULTATION_CREATED":
                return "Menunggu konfirmasi dari konsultan";
            case "CONSULTATION_ACCEPTED":
                return "Jane Consultant telah menerima permintaan konsultasi";
            case "SURVEY_SCHEDULED":
                return "Jadwal: 16 Jan 2024, 10:00 - Biaya: Rp " + (event.getSurveyCost() != null ? event.getSurveyCost().toString() : "N/A");
            case "SURVEY_APPROVED":
                return "Survey akan dilaksanakan pada tanggal 18 Januari 2024";
            case "SURVEY_REJECTED":
                return "Homeowner mengusulkan jadwal baru";
            case "SURVEY_RESCHEDULED":
                return "Jadwal baru: 18 Jan 2024, 14:00 - Biaya tetap";
            case "SURVEY_COMPLETED":
                return "Survey telah berhasil dilaksanakan - Total biaya: Rp " + (event.getSurveyCost() != null ? event.getSurveyCost().toString() : "N/A");
            case "CONTRACT_DRAFT_CREATED":
                return "Kontrak konsultasi desain rumah minimalis";
            case "CONTRACT_REQUEST_FOR_APPROVAL":
                return "Konsultan mengirim draft kontrak untuk persetujuan homeowner";
            case "CONTRACT_APPROVED":
                return "Kontrak siap untuk ditandatangani";
            case "CONTRACT_REJECTED":
                return "Homeowner menolak kontrak dengan alasan tertentu";
            case "CONTRACT_REVISION_REQUESTED":
                return "Homeowner meminta revisi kontrak";
            case "PAYMENT_REQUESTED":
                return "Konsultan meminta pembayaran termin pertama";
            case "PAYMENT_APPROVED":
                return "Homeowner menyetujui pembayaran";
            case "CONSULTATION_STARTED":
                return "Konsultasi dimulai setelah pembayaran disetujui";
            case "DOCUMENT_DOCUMENTS_UPLOADED":
                return "Konsultan mengupload semua dokumen teknis proyek";
            case "DOCUMENT_REVISION_REQUESTED":
                return "Revisi pada layout dapur dan penyesuaian budget";
            case "DOCUMENT_DOCUMENTS_REVISED":
                return "DED dan RAB telah disesuaikan sesuai feedback";
            case "DOCUMENT_DOCUMENT_FINAL":
                return "DED, BOQ, dan RAB versi final telah disetujui homeowner";
            default:
                return "Event: " + event.getEventType();
        }
    }

    private String generateNotes(ConsultationEvent event) {
        StringBuilder notes = new StringBuilder();

        if (event.getSurveyCost() != null) {
            notes.append("Biaya survey: Rp ").append(event.getSurveyCost().toString());
        }

        if (event.getSurveyLocation() != null && !event.getSurveyLocation().isEmpty()) {
            if (notes.length() > 0) notes.append(" - ");
            notes.append("Lokasi: ").append(event.getSurveyLocation());
        }

        switch (event.getStep() + "_" + event.getState()) {
            case "CONSULTATION_CREATED":
                notes.append("Project: Rumah Minimalis Modern");
                break;
            case "CONSULTATION_ACCEPTED":
                notes.append("Konsultasi siap untuk dilanjutkan ke tahap survey");
                break;
            case "SURVEY_SCHEDULED":
                notes.append("Menunggu approval dari homeowner");
                break;
            case "SURVEY_APPROVED":
                notes.append("Jadwal survey telah final");
                break;
            case "CONTRACT_REQUEST_FOR_APPROVAL":
                notes.append("Deadline approval: 7 hari");
                break;
            case "CONTRACT_REVISION_REQUESTED":
                notes.append("Homeowner meminta revisi kontrak");
                break;
            case "PAYMENT_REQUESTED":
                notes.append("Konsultan meminta pembayaran termin pertama setelah kontrak ditandatangani");
                break;
            case "PAYMENT_APPROVED":
                notes.append("Homeowner telah menyetujui pembayaran dan dana ditahan di escrow");
                break;
            case "CONSULTATION_STARTED":
                notes.append("Konsultasi resmi dimulai setelah pembayaran disetujui homeowner");
                break;
            case "DOCUMENT_DOCUMENTS_UPLOADED":
                notes.append("DED: Detail Engineering Design, BOQ: Bill of Quantities, RAB: Rencana Anggaran Biaya");
                break;
            case "DOCUMENT_DOCUMENT_FINAL":
                notes.append("Semua dokumen telah final dan siap untuk permit application");
                break;
        }

        return notes.toString();
    }
}