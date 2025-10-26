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
                                  String title, String subtitle, List<String> files,
                                  List<String> attachments, String actor, String status,
                                  String notes) {
        log.debug("Creating history event for consultation ID: {}, step: {}, state: {}", consultation.getId(), step, state);
        createHistoryEvent(consultation, step, state, title, subtitle, files, attachments,
                          actor, status, notes, null, null);
    }

    @Transactional
    public void createHistoryEvent(Consultation consultation, String step, String state,
                                  String title, String subtitle, List<String> files,
                                  List<String> attachments, String actor, String status,
                                  String notes, BigDecimal surveyCost, String surveyLocation) {

        ConsultationHistory history = new ConsultationHistory();
        history.setConsultation(consultation);
        history.setStep(step);
        history.setState(state);
        history.setTitle(title);
        history.setSubtitle(subtitle);
        history.setFiles(files);
        history.setAttachments(attachments);
        history.setActor(actor);
        history.setStatus(status);
        history.setNotes(notes);
        history.setSurveyCost(surveyCost);
        history.setSurveyLocation(surveyLocation);
        history.setDateTime(LocalDateTime.now());

        try {
            historyRepository.save(history);
            log.info("History event created for consultation ID: {}, step: {}, actor: {}", consultation.getId(), step, actor);
        } catch (Exception e) {
            log.error("Failed to create history event for consultation ID: {}", consultation.getId(), e);
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
}