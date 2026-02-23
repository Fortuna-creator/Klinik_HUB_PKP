package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.SurveySchedule;
import com.hub.app.consultation.model.SurveyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurveyScheduleRepository extends JpaRepository<SurveySchedule, UUID> {

    List<SurveySchedule> findByConsultationIdOrderByCreatedAtDesc(UUID consultationId);

    Optional<SurveySchedule> findByConsultationIdAndStatus(UUID consultationId, SurveyStatus status);
}