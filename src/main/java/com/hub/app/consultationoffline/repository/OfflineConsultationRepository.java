package com.hub.app.consultationoffline.repository;

import com.hub.app.consultationoffline.model.ConsultationStatus;
import com.hub.app.consultationoffline.model.OfflineConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

    /**
     * Get all booked time slots for a specific balai and date
     * excluding cancelled consultations
     * Used for: Calendar time slot availability
     */
    @Query("SELECT oc.consultationTime FROM OfflineConsultation oc " +
           "WHERE oc.balai.id = :balaiId " +
           "AND oc.consultationDate = :date " +
           "AND oc.status != 'CANCELLED'")
    List<LocalTime> findBookedTimeSlotsByBalaiAndDate(
            @Param("balaiId") UUID balaiId,
            @Param("date") LocalDate date
    );

    /**
     * Get all consultations for a specific user
     * Ordered by consultation date descending (newest first)
     * Used for: User's booking history / My Bookings page
     */
    List<OfflineConsultation> findByUserIdOrderByConsultationDateDescConsultationTimeDesc(Long userId);

    /**
     * Get all consultations for a specific balai
     * Ordered by consultation date descending
     * Used for: Balai-specific consultation list (admin view)
     */
    List<OfflineConsultation> findByBalaiIdOrderByConsultationDateDescConsultationTimeDesc(UUID balaiId);

    /**
     * Get all consultations for a specific balai and date
     * Excluding cancelled consultations
     * Used for: Daily schedule view
     */
    @Query("SELECT oc FROM OfflineConsultation oc " +
           "WHERE oc.balai.id = :balaiId " +
           "AND oc.consultationDate = :date " +
           "AND oc.status != 'CANCELLED' " +
           "ORDER BY oc.consultationTime ASC")
    List<OfflineConsultation> findByBalaiIdAndDateExcludingCancelled(
            @Param("balaiId") UUID balaiId,
            @Param("date") LocalDate date
    );

    /**
     * Get all distinct dates that have bookings for a specific balai within a date range
     * Excluding cancelled consultations
     * Used for: Calendar month view - highlight dates with bookings
     */
    @Query("SELECT DISTINCT oc.consultationDate FROM OfflineConsultation oc " +
           "WHERE oc.balai.id = :balaiId " +
           "AND oc.consultationDate BETWEEN :startDate AND :endDate " +
           "AND oc.status != 'CANCELLED' " +
           "ORDER BY oc.consultationDate ASC")
    List<LocalDate> findBookedDatesByBalaiAndDateRange(
            @Param("balaiId") UUID balaiId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Count bookings for a specific balai and date
     * Excluding cancelled consultations
     * Used for: Check if date is fully booked
     */
    @Query("SELECT COUNT(oc) FROM OfflineConsultation oc " +
           "WHERE oc.balai.id = :balaiId " +
           "AND oc.consultationDate = :date " +
           "AND oc.status != 'CANCELLED'")
    long countBookingsByBalaiAndDate(
            @Param("balaiId") UUID balaiId,
            @Param("date") LocalDate date
    );

    /**
     * Get consultations by user and status
     * Used for: Filter user bookings by status (pending, confirmed, etc)
     */
    List<OfflineConsultation> findByUserIdAndStatusOrderByConsultationDateDescConsultationTimeDesc(
            Long userId,
            ConsultationStatus status
    );

    /**
     * Get consultations by balai and status
     * Used for: Admin view - filter by status
     */
    List<OfflineConsultation> findByBalaiIdAndStatusOrderByConsultationDateDescConsultationTimeDesc(
            UUID balaiId,
            ConsultationStatus status
    );

    /**
     * Get upcoming consultations for a user (future dates only)
     * Used for: Show upcoming appointments
     */
    @Query("SELECT oc FROM OfflineConsultation oc " +
           "WHERE oc.user.id = :userId " +
           "AND oc.consultationDate >= :currentDate " +
           "AND oc.status NOT IN ('CANCELLED', 'COMPLETED') " +
           "ORDER BY oc.consultationDate ASC, oc.consultationTime ASC")
    List<OfflineConsultation> findUpcomingConsultationsByUser(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * Get past consultations for a user
     * Used for: Consultation history
     */
    @Query("SELECT oc FROM OfflineConsultation oc " +
           "WHERE oc.user.id = :userId " +
           "AND (oc.consultationDate < :currentDate " +
           "     OR (oc.consultationDate = :currentDate AND oc.consultationTime < :currentTime)) " +
           "ORDER BY oc.consultationDate DESC, oc.consultationTime DESC")
    List<OfflineConsultation> findPastConsultationsByUser(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime
    );
}

