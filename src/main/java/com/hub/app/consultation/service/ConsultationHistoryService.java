package com.hub.app.consultation.service;

import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationHistory;
import com.hub.app.consultation.repository.ConsultationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultationHistoryService {

    private final ConsultationHistoryRepository historyRepository;

    @Transactional
    public void createHistoryEvent(Consultation consultation, String step, String state,
                                 String title, String subtitle, List<String> files,
                                 List<String> attachments, String actor, String status,
                                 String notes) {
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

        historyRepository.save(history);
    }

    public List<ConsultationHistory> getConsultationHistory(UUID consultationId) {
        return historyRepository.findByConsultationIdOrderByDateTimeDesc(consultationId);
    }

    public List<ConsultationHistory> getHistoryByStep(UUID consultationId, String step) {
        return historyRepository.findByConsultationIdAndStepOrderByDateTimeDesc(consultationId, step);
    }

    public List<ConsultationHistory> getHistoryByStepAndState(UUID consultationId, String step, String state) {
        return historyRepository.findByConsultationIdAndStepAndStateOrderByDateTimeDesc(consultationId, step, state);
    }
}