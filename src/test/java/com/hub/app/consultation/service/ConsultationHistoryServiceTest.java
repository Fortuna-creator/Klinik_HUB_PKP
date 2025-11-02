package com.hub.app.consultation.service;

import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationHistory;
import com.hub.app.consultation.repository.ConsultationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultationHistoryServiceTest {

    @Mock
    private ConsultationHistoryRepository historyRepository;

    @InjectMocks
    private ConsultationHistoryService historyService;

    private Consultation consultation;
    private UUID consultationId;

    @BeforeEach
    void setUp() {
        consultationId = UUID.randomUUID();
        consultation = new Consultation();
        consultation.setId(consultationId);
    }

    @Test
    void createHistoryEvent_ShouldSaveHistory() {
        // Given
        String step = "CONSULTATION";
        String state = "CREATED";
        String title = "Consultation created";
        String subtitle = "New consultation request";
        List<UUID> files = Arrays.asList(UUID.randomUUID());
        List<String> attachments = Arrays.asList("attachment1.pdf");
        String actor = "HOMEOWNER";
        String status = "PENDING";
        String notes = "Test notes";

        ConsultationHistory savedHistory = new ConsultationHistory();
        savedHistory.setId(UUID.randomUUID());
        savedHistory.setStep(step);
        savedHistory.setState(state);
        savedHistory.setTitle(title);

        when(historyRepository.save(any(ConsultationHistory.class))).thenReturn(savedHistory);

        // When
        historyService.createHistoryEvent(consultation, step, state, title, subtitle,
                                        files, attachments, actor, status, notes);

        // Then
        verify(historyRepository).save(any(ConsultationHistory.class));
    }

    @Test
    void createHistoryEventWithSurveyCost_ShouldSaveHistoryWithCost() {
        // Given
        BigDecimal surveyCost = new BigDecimal("1500000.00");
        String surveyLocation = "Jakarta Selatan";

        ConsultationHistory savedHistory = new ConsultationHistory();
        savedHistory.setId(UUID.randomUUID());
        savedHistory.setSurveyCost(surveyCost);
        savedHistory.setSurveyLocation(surveyLocation);

        when(historyRepository.save(any(ConsultationHistory.class))).thenReturn(savedHistory);

        // When
        historyService.createHistoryEvent(consultation, "SURVEY", "SCHEDULED",
                                        "Survey scheduled", "Survey proposal",
                                        Arrays.asList(), Arrays.asList(),
                                        "CONSULTANT", "PENDING", "Survey notes",
                                        surveyCost, surveyLocation);

        // Then
        verify(historyRepository).save(any(ConsultationHistory.class));
    }

    @Test
    void getConsultationHistory_ShouldReturnHistoryList() {
        // Given
        List<ConsultationHistory> expectedHistory = Arrays.asList(
            createHistory("CONSULTATION", "CREATED"),
            createHistory("SURVEY", "SCHEDULED")
        );

        when(historyRepository.findByConsultationIdOrderByDateTimeDesc(consultationId))
            .thenReturn(expectedHistory);

        // When
        List<ConsultationHistory> result = historyService.getConsultationHistory(consultationId);

        // Then
        assertThat(result).isEqualTo(expectedHistory);
        verify(historyRepository).findByConsultationIdOrderByDateTimeDesc(consultationId);
    }

    @Test
    void getHistoryByStep_ShouldReturnFilteredHistory() {
        // Given
        String step = "SURVEY";
        List<ConsultationHistory> expectedHistory = Arrays.asList(
            createHistory("SURVEY", "SCHEDULED"),
            createHistory("SURVEY", "APPROVED")
        );

        when(historyRepository.findByConsultationIdAndStepOrderByDateTimeDesc(consultationId, step))
            .thenReturn(expectedHistory);

        // When
        List<ConsultationHistory> result = historyService.getHistoryByStep(consultationId, step);

        // Then
        assertThat(result).isEqualTo(expectedHistory);
        verify(historyRepository).findByConsultationIdAndStepOrderByDateTimeDesc(consultationId, step);
    }

    @Test
    void getHistoryByStepAndState_ShouldReturnFilteredHistory() {
        // Given
        String step = "SURVEY";
        String state = "APPROVED";
        List<ConsultationHistory> expectedHistory = Arrays.asList(
            createHistory("SURVEY", "APPROVED")
        );

        when(historyRepository.findByConsultationIdAndStepAndStateOrderByDateTimeDesc(consultationId, step, state))
            .thenReturn(expectedHistory);

        // When
        List<ConsultationHistory> result = historyService.getHistoryByStepAndState(consultationId, step, state);

        // Then
        assertThat(result).isEqualTo(expectedHistory);
        verify(historyRepository).findByConsultationIdAndStepAndStateOrderByDateTimeDesc(consultationId, step, state);
    }

    private ConsultationHistory createHistory(String step, String state) {
        ConsultationHistory history = new ConsultationHistory();
        history.setId(UUID.randomUUID());
        history.setConsultation(consultation);
        history.setStep(step);
        history.setState(state);
        history.setTitle(step + " " + state);
        history.setDateTime(LocalDateTime.now());
        return history;
    }
}