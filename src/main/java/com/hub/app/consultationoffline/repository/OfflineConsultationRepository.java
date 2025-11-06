package com.hub.app.consultationoffline.repository;

import com.hub.app.consultationoffline.model.OfflineConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface OfflineConsultationRepository extends JpaRepository<OfflineConsultation, UUID> {

    /**
     * Check if a booking already exists for the same user, balai, date, and time
     * excluding cancelled consultations
     */
    @Query("SELECT COUNT(oc) > 0 FROM OfflineConsultation oc " +
           "WHERE oc.user.id = :userId " +
           "AND oc.balai.id = :balaiId " +
           "AND oc.consultationDate = :date " +
           "AND oc.consultationTime = :time " +
           "AND oc.status != 'CANCELLED'")
    boolean existsByUserAndBalaiAndDateAndTime(
            @Param("userId") Long userId,
            @Param("balaiId") UUID balaiId,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time
    );
}

