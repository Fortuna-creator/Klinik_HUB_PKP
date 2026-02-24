package com.hub.app.offlineconsultation.repository;

import com.hub.app.offlineconsultation.entity.ConsultationStatus;
import com.hub.app.offlineconsultation.entity.OfflineConsultationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface OcConsultationRepository extends JpaRepository<OfflineConsultationEntity, UUID> {

    // ── Warga: my consultations ──────────────────────────────────────────

    Page<OfflineConsultationEntity> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT c FROM OfflineConsultationEntity c WHERE c.user.id = :userId AND c.status = :status")
    Page<OfflineConsultationEntity> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") ConsultationStatus status,
            Pageable pageable);

    // ── Dashboard: Balai consultations ───────────────────────────────────

    Page<OfflineConsultationEntity> findByBalaiId(UUID balaiId, Pageable pageable);

    @Query("SELECT c FROM OfflineConsultationEntity c WHERE c.balai.id = :balaiId AND c.status = :status")
    Page<OfflineConsultationEntity> findByBalaiIdAndStatus(
            @Param("balaiId") UUID balaiId,
            @Param("status") ConsultationStatus status,
            Pageable pageable);

    @Query("SELECT c FROM OfflineConsultationEntity c WHERE c.balai.id = :balaiId AND c.consultationDate = :date")
    Page<OfflineConsultationEntity> findByBalaiIdAndDate(
            @Param("balaiId") UUID balaiId,
            @Param("date") LocalDate date,
            Pageable pageable);

    @Query("SELECT c FROM OfflineConsultationEntity c WHERE c.balai.id = :balaiId AND c.consultationDate = :date AND c.status = :status")
    Page<OfflineConsultationEntity> findByBalaiIdAndDateAndStatus(
            @Param("balaiId") UUID balaiId,
            @Param("date") LocalDate date,
            @Param("status") ConsultationStatus status,
            Pageable pageable);

    // ── Slot availability ────────────────────────────────────────────────

    /** Count active (non-cancelled) bookings for a specific balai + date + time slot. */
    @Query("SELECT COUNT(c) FROM OfflineConsultationEntity c " +
           "WHERE c.balai.id = :balaiId " +
           "AND c.consultationDate = :date " +
           "AND c.timeSlot = :timeSlot " +
           "AND c.status <> :excludedStatus")
    long countActiveByBalaiAndDateAndSlot(
            @Param("balaiId") UUID balaiId,
            @Param("date") LocalDate date,
            @Param("timeSlot") String timeSlot,
            @Param("excludedStatus") ConsultationStatus excludedStatus);

    /** Fetch all active bookings for a balai + date (used for building slot list). */
    @Query("SELECT c FROM OfflineConsultationEntity c " +
           "WHERE c.balai.id = :balaiId " +
           "AND c.consultationDate = :date " +
           "AND c.status <> :excludedStatus")
    List<OfflineConsultationEntity> findActiveByBalaiAndDate(
            @Param("balaiId") UUID balaiId,
            @Param("date") LocalDate date,
            @Param("excludedStatus") ConsultationStatus excludedStatus);

    // ── Dashboard stats ──────────────────────────────────────────────────

    @Query("SELECT COUNT(c) FROM OfflineConsultationEntity c " +
           "WHERE c.balai.id = :balaiId AND c.consultationDate = :date")
    long countByBalaiAndDate(@Param("balaiId") UUID balaiId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(c) FROM OfflineConsultationEntity c " +
           "WHERE c.balai.id = :balaiId AND c.consultationDate = :date AND c.status = :status")
    long countByBalaiAndDateAndStatus(
            @Param("balaiId") UUID balaiId,
            @Param("date") LocalDate date,
            @Param("status") ConsultationStatus status);
}
