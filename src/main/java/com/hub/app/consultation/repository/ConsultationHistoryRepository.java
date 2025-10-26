package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.ConsultationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConsultationHistoryRepository extends JpaRepository<ConsultationHistory, UUID> {

    List<ConsultationHistory> findByConsultationIdOrderByDateTimeDesc(UUID consultationId);

    @Query("SELECT ch FROM ConsultationHistory ch WHERE ch.consultation.id = :consultationId AND ch.step = :step ORDER BY ch.dateTime DESC")
    List<ConsultationHistory> findByConsultationIdAndStepOrderByDateTimeDesc(
        @Param("consultationId") UUID consultationId,
        @Param("step") String step);

    @Query("SELECT ch FROM ConsultationHistory ch WHERE ch.consultation.id = :consultationId AND ch.step = :step AND ch.state = :state ORDER BY ch.dateTime DESC")
    List<ConsultationHistory> findByConsultationIdAndStepAndStateOrderByDateTimeDesc(
        @Param("consultationId") UUID consultationId,
        @Param("step") String step,
        @Param("state") String state);
}