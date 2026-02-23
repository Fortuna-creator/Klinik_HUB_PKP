package com.hub.app.consultation.service;

import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationHistory;
import com.hub.app.consultation.repository.ConsultationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationHistoryService {

    private final ConsultationHistoryRepository historyRepository;

    @Transactional
    public void createHistoryEvent(Consultation consultation, String step, String state,
                                   String title, String subtitle, List<UUID> fileIds,
                                   List<String> attachments, String actor, String status,
                                   String notes) {
        log.debug("Creating history event for consultation ID: {}, step: {}, state: {}", consultation.getId(), step, state);
        createHistoryEvent(consultation, step, state, title, subtitle, fileIds, attachments,
                           actor, status, notes, null, null);
    }

    @Transactional
    public void createHistoryEvent(Consultation consultation, String step, String state,
                                   String title, String subtitle, List<UUID> fileIds,
                                   List<String> attachments, String actor, String status,
                                   String notes, BigDecimal surveyCost, String surveyLocation) {
        createHistoryEvent(consultation, step, state, title, subtitle, fileIds, attachments,
                           actor, status, notes, surveyCost, surveyLocation, null);
    }

    @Transactional
    public void createHistoryEvent(Consultation consultation, String step, String state,
                                   String title, String subtitle, List<UUID> fileIds,
                                   List<String> attachments, String actor, String status,
                                   String notes, BigDecimal surveyCost, String surveyLocation,
                                   UUID scheduleId) {
        createHistoryEvent(consultation, step, state, title, subtitle, fileIds, attachments,
                           actor, status, notes, surveyCost, surveyLocation, scheduleId, null);
    }

    @Transactional
    public void createHistoryEvent(Consultation consultation, String step, String state,
                                   String title, String subtitle, List<UUID> fileIds,
                                   List<String> attachments, String actor, String status,
                                   String notes, BigDecimal surveyCost, String surveyLocation,
                                   UUID scheduleId, UUID contractId) {
        createHistoryEvent(consultation, step, state, title, subtitle, fileIds, attachments,
                          actor, status, notes, surveyCost, surveyLocation, scheduleId, contractId, null);
    }

    @Transactional
    public void createHistoryEvent(Consultation consultation, String step, String state,
                                    String title, String subtitle, List<UUID> fileIds,
                                    List<String> attachments, String actor, String status,
                                    String notes, BigDecimal surveyCost, String surveyLocation,
                                    UUID scheduleId, UUID contractId, UUID paymentId) {
        createHistoryEvent(consultation, step, state, title, subtitle, fileIds, attachments,
                          actor, status, notes, surveyCost, surveyLocation, scheduleId, contractId, paymentId, null);
    }

    @Transactional
    public void createHistoryEvent(Consultation consultation, String step, String state,
                                    String title, String subtitle, List<UUID> fileIds,
                                    List<String> attachments, String actor, String status,
                                    String notes, BigDecimal surveyCost, String surveyLocation,
                                    UUID scheduleId, UUID contractId, UUID paymentId, BigDecimal totalPaymentAmount) {

        log.info("=== STARTING HISTORY EVENT CREATION ===");
        log.info("Consultation ID: {}, Step: {}, State: {}, Actor: {}", consultation.getId(), step, state, actor);
        log.info("Title: {}, Survey Cost: {}, Survey Location: {}, Schedule ID: {}, Contract ID: {}, Payment ID: {}, Total Payment Amount: {}",
                 title, surveyCost, surveyLocation, scheduleId, contractId, paymentId, totalPaymentAmount);

        ConsultationHistory history = new ConsultationHistory();
        history.setConsultation(consultation);
        history.setStep(step);
        history.setState(state);
        history.setTitle(title);
        history.setSubtitle(subtitle);
        history.setFiles(null); // Keep old field for backward compatibility
        history.setFileIds(fileIds);
        history.setAttachments(attachments);
        history.setActor(actor);
        history.setStatus(status);
        history.setNotes(notes);
        history.setSurveyCost(surveyCost);
        history.setSurveyLocation(surveyLocation);
        history.setScheduleId(scheduleId);
        history.setContractId(contractId);
        history.setPaymentId(paymentId);
        history.setTotalPaymentAmount(totalPaymentAmount);
        history.setDateTime(LocalDateTime.now());

        log.debug("History entity prepared: {}", history);

        try {
            ConsultationHistory saved = historyRepository.save(history);
            log.info("=== HISTORY EVENT CREATED SUCCESSFULLY ===");
            log.info("Saved history ID: {}, Consultation ID: {}, Step: {}, State: {}", saved.getId(), consultation.getId(), step, state);
        } catch (Exception e) {
            log.error("=== HISTORY EVENT CREATION FAILED ===");
            log.error("Consultation ID: {}, Step: {}, State: {}, Error: {}", consultation.getId(), step, state, e.getMessage(), e);
            throw e;
        }
    }

    public List<ConsultationHistory> getConsultationHistory(UUID consultationId) {
        log.debug("Retrieving consultation history for consultation ID: {}", consultationId);
        List<ConsultationHistory> history = historyRepository.findByConsultationIdOrderByDateTimeDesc(consultationId);
        log.debug("Retrieved {} history events for consultation ID: {}", history.size(), consultationId);
        return history;
    }

    public List<ConsultationHistory> getHistoryByStep(UUID consultationId, String step) {
        log.debug("Retrieving history by step '{}' for consultation ID: {}", step, consultationId);
        List<ConsultationHistory> history = historyRepository.findByConsultationIdAndStepOrderByDateTimeDesc(consultationId, step);
        log.debug("Retrieved {} history events for step '{}' and consultation ID: {}", history.size(), step, consultationId);
        return history;
    }

    public List<ConsultationHistory> getHistoryByStepAndState(UUID consultationId, String step, String state) {
        log.debug("Retrieving history by step '{}' and state '{}' for consultation ID: {}", step, state, consultationId);
        List<ConsultationHistory> history = historyRepository.findByConsultationIdAndStepAndStateOrderByDateTimeDesc(consultationId, step, state);
        log.debug("Retrieved {} history events for step '{}', state '{}' and consultation ID: {}", history.size(), step, state, consultationId);
        return history;
    }

    public List<ConsultationHistory> getContractHistory(UUID contractId) {
        log.debug("Retrieving contract history for contract ID: {}", contractId);
        List<ConsultationHistory> history = historyRepository.findByContractIdOrderByDateTimeDesc(contractId);
        log.debug("Retrieved {} history events for contract ID: {}", history.size(), contractId);
        return history;
    }
}