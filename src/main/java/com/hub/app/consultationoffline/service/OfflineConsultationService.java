package com.hub.app.consultationoffline.service;

import com.hub.app.balaioffline.model.Balai;
import com.hub.app.balaioffline.repository.BalaiRepository;
import com.hub.app.consultationoffline.dto.*;
import com.hub.app.consultationoffline.model.ConsultationStatus;
import com.hub.app.consultationoffline.model.OfflineConsultation;
import com.hub.app.consultationoffline.repository.OfflineConsultationRepository;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfflineConsultationService {

    private static final Logger logger = LoggerFactory.getLogger(OfflineConsultationService.class);

    private final OfflineConsultationRepository consultationRepository;
    private final UserRepository userRepository;
    private final BalaiRepository balaiRepository;

    private static final LocalTime BUSINESS_HOURS_START = LocalTime.of(8, 0);
    private static final LocalTime BUSINESS_HOURS_END = LocalTime.of(17, 0);
    private static final int DEFAULT_SLOT_INTERVAL_MINUTES = 60;

    @Transactional
    public OfflineConsultationResponse createConsultation(CreateOfflineConsultationRequest request) {
        logger.info("Creating consultation for user {} at balai {}", request.getUserId(), request.getBalaiId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        // Validate balai exists
        UUID balaiId = UUID.fromString(request.getBalaiId());
        Balai balai = balaiRepository.findById(balaiId)
                .orElseThrow(() -> new DomainException(ErrorCode.BALAI_NOT_FOUND));

        // Validate date is not in the past
        if (request.getConsultationDate().isBefore(LocalDate.now())) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_DATE);
        }

        // Validate time is within business hours
        if (request.getConsultationTime().isBefore(BUSINESS_HOURS_START) ||
            request.getConsultationTime().isAfter(BUSINESS_HOURS_END)) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_TIME);
        }

        // Check for duplicate booking
        boolean exists = consultationRepository.existsByUserAndBalaiAndDateAndTime(
                request.getUserId(),
                balaiId,
                request.getConsultationDate(),
                request.getConsultationTime()
        );

        if (exists) {
            throw new DomainException(ErrorCode.CONSULTATION_ALREADY_BOOKED);
        }

        // Create consultation
        OfflineConsultation consultation = OfflineConsultation.builder()
                .user(user)
                .balai(balai)
                .consultationDate(request.getConsultationDate())
                .consultationTime(request.getConsultationTime())
                .status(ConsultationStatus.PENDING)
                .build();

        consultation = consultationRepository.save(consultation);
        logger.info("Successfully created consultation with id: {}", consultation.getId());

        return mapToResponse(consultation);
    }

    @Transactional(readOnly = true)
    public List<OfflineConsultationResponse> getAllConsultations() {
        logger.info("Fetching all consultations");
        List<OfflineConsultation> consultations = consultationRepository.findAll();
        return consultations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OfflineConsultationResponse getConsultationById(UUID id) {
        logger.info("Fetching consultation by id: {}", id);
        OfflineConsultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.OFFLINE_CONSULTATION_NOT_FOUND));
        return mapToResponse(consultation);
    }

    @Transactional
    public OfflineConsultationResponse updateConsultationStatus(UUID id, UpdateConsultationStatusRequest request) {
        logger.info("Updating consultation {} status to {}", id, request.getStatus());

        OfflineConsultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.OFFLINE_CONSULTATION_NOT_FOUND));

        ConsultationStatus newStatus;
        try {
            newStatus = ConsultationStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_STATUS, "Invalid status value");
        }

        // Validate status transition
        validateStatusTransition(consultation.getStatus(), newStatus);

        consultation.setStatus(newStatus);
        consultation = consultationRepository.save(consultation);

        logger.info("Successfully updated consultation status");
        return mapToResponse(consultation);
    }

    @Transactional
    public void deleteConsultation(UUID id) {
        logger.info("Deleting consultation: {}", id);

        if (!consultationRepository.existsById(id)) {
            throw new DomainException(ErrorCode.OFFLINE_CONSULTATION_NOT_FOUND);
        }

        consultationRepository.deleteById(id);
        logger.info("Successfully deleted consultation");
    }

    /**
     * Get available time slots untuk tanggal dan balai tertentu
     * Digunakan untuk: Calendar time picker
     */
    @Transactional(readOnly = true)
    public AvailableSlotsResponse getAvailableSlots(UUID balaiId, LocalDate date, Integer slotIntervalMinutes) {
        logger.info("Fetching available slots for balai {} on {}", balaiId, date);

        // Validate balai exists
        Balai balai = balaiRepository.findById(balaiId)
                .orElseThrow(() -> new DomainException(ErrorCode.BALAI_NOT_FOUND));

        // Validate date is not in the past
        if (date.isBefore(LocalDate.now())) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_DATE, 
                    "Cannot check availability for past dates");
        }

        // Use default interval if not provided
        int interval = slotIntervalMinutes != null ? slotIntervalMinutes : DEFAULT_SLOT_INTERVAL_MINUTES;

        // Generate all possible time slots
        List<String> allSlots = generateTimeSlots(interval);

        // Get booked slots
        List<LocalTime> bookedTimes = consultationRepository.findBookedTimeSlotsByBalaiAndDate(balaiId, date);
        List<String> bookedSlots = bookedTimes.stream()
                .map(LocalTime::toString)
                .collect(Collectors.toList());

        // Calculate available slots
        List<String> availableSlots = allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());

        logger.info("Found {} available slots and {} booked slots", availableSlots.size(), bookedSlots.size());

        return AvailableSlotsResponse.builder()
                .date(date)
                .balaiId(balaiId)
                .balaiName(balai.getName())
                .businessHours(BusinessHours.builder()
                        .start(BUSINESS_HOURS_START.toString())
                        .end(BUSINESS_HOURS_END.toString())
                        .slotIntervalMinutes(interval)
                        .build())
                .availableSlots(availableSlots)
                .bookedSlots(bookedSlots)
                .totalAvailableSlots(availableSlots.size())
                .totalBookedSlots(bookedSlots.size())
                .build();
    }

    /**
     * Get booked dates calendar untuk bulan tertentu
     * Digunakan untuk: Calendar month view
     */
    @Transactional(readOnly = true)
    public BookedDatesCalendarResponse getBookedDatesCalendar(UUID balaiId, int month, int year) {
        logger.info("Fetching booked dates for balai {} in {}/{}", balaiId, month, year);

        // Validate balai exists
        Balai balai = balaiRepository.findById(balaiId)
                .orElseThrow(() -> new DomainException(ErrorCode.BALAI_NOT_FOUND));

        // Calculate date range for the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get all booked dates in the month
        List<LocalDate> bookedDates = consultationRepository.findBookedDatesByBalaiAndDateRange(
                balaiId, startDate, endDate);

        // Calculate total slots per day (based on 60-minute intervals)
        int totalSlotsPerDay = calculateTotalSlots(DEFAULT_SLOT_INTERVAL_MINUTES);

        // Build response for each booked date
        List<BookedDateResponse> dateResponses = new ArrayList<>();
        int totalFullyBooked = 0;

        for (LocalDate date : bookedDates) {
            long bookingCount = consultationRepository.countBookingsByBalaiAndDate(balaiId, date);
            int availableSlots = totalSlotsPerDay - (int) bookingCount;
            boolean isFullyBooked = availableSlots <= 0;
            double bookingPercentage = (bookingCount * 100.0) / totalSlotsPerDay;

            if (isFullyBooked) {
                totalFullyBooked++;
            }

            dateResponses.add(BookedDateResponse.builder()
                    .date(date)
                    .totalBookings((int) bookingCount)
                    .availableSlots(Math.max(0, availableSlots))
                    .totalSlots(totalSlotsPerDay)
                    .isFullyBooked(isFullyBooked)
                    .bookingPercentage(Math.round(bookingPercentage * 100.0) / 100.0)
                    .build());
        }

        logger.info("Found {} days with bookings, {} fully booked", bookedDates.size(), totalFullyBooked);

        return BookedDatesCalendarResponse.builder()
                .balaiId(balaiId)
                .balaiName(balai.getName())
                .month(month)
                .year(year)
                .dates(dateResponses)
                .totalDaysWithBookings(bookedDates.size())
                .totalFullyBookedDays(totalFullyBooked)
                .build();
    }

    /**
     * Get user consultation history (upcoming & past)
     * Digunakan untuk: My Bookings page
     */
    @Transactional(readOnly = true)
    public UserConsultationHistoryResponse getUserConsultationHistory(Long userId) {
        logger.info("Fetching consultation history for user {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        // Get upcoming consultations
        List<OfflineConsultation> upcomingConsultations = consultationRepository
                .findUpcomingConsultationsByUser(userId, LocalDate.now());

        // Get past consultations
        List<OfflineConsultation> pastConsultations = consultationRepository
                .findPastConsultationsByUser(userId, LocalDate.now(), LocalTime.now());

        // Calculate status summary for all consultations
        List<OfflineConsultation> allConsultations = consultationRepository
                .findByUserIdOrderByConsultationDateDescConsultationTimeDesc(userId);

        ConsultationStatusSummary statusSummary = calculateStatusSummary(allConsultations);

        logger.info("Found {} upcoming and {} past consultations", 
                upcomingConsultations.size(), pastConsultations.size());

        return UserConsultationHistoryResponse.builder()
                .userId(userId)
                .userName(user.getName())
                .upcomingConsultations(upcomingConsultations.stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .pastConsultations(pastConsultations.stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .totalUpcoming(upcomingConsultations.size())
                .totalPast(pastConsultations.size())
                .statusSummary(statusSummary)
                .build();
    }

    /**
     * Get daily schedule untuk balai tertentu
     * Digunakan untuk: Daily schedule view
     */
    @Transactional(readOnly = true)
    public DailyScheduleResponse getDailySchedule(UUID balaiId, LocalDate date) {
        logger.info("Fetching daily schedule for balai {} on {}", balaiId, date);

        // Validate balai exists
        Balai balai = balaiRepository.findById(balaiId)
                .orElseThrow(() -> new DomainException(ErrorCode.BALAI_NOT_FOUND));

        // Get consultations for the day
        List<OfflineConsultation> consultations = consultationRepository
                .findByBalaiIdAndDateExcludingCancelled(balaiId, date);

        // Calculate available slots
        int totalSlotsPerDay = calculateTotalSlots(DEFAULT_SLOT_INTERVAL_MINUTES);
        int availableSlots = totalSlotsPerDay - consultations.size();

        logger.info("Found {} consultations for the day, {} slots available", 
                consultations.size(), availableSlots);

        return DailyScheduleResponse.builder()
                .balaiId(balaiId)
                .balaiName(balai.getName())
                .date(date)
                .consultations(consultations.stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .totalConsultations(consultations.size())
                .availableSlots(Math.max(0, availableSlots))
                .businessHours(BusinessHours.builder()
                        .start(BUSINESS_HOURS_START.toString())
                        .end(BUSINESS_HOURS_END.toString())
                        .slotIntervalMinutes(DEFAULT_SLOT_INTERVAL_MINUTES)
                        .build())
                .build();
    }

    /**
     * Get consultations by user ID
     */
    @Transactional(readOnly = true)
    public List<OfflineConsultationResponse> getConsultationsByUserId(Long userId) {
        logger.info("Fetching consultations for user {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new DomainException(ErrorCode.USER_NOT_FOUND);
        }

        List<OfflineConsultation> consultations = consultationRepository
                .findByUserIdOrderByConsultationDateDescConsultationTimeDesc(userId);

        logger.info("Found {} consultations for user", consultations.size());

        return consultations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get consultations by balai ID
     */
    @Transactional(readOnly = true)
    public List<OfflineConsultationResponse> getConsultationsByBalaiId(UUID balaiId) {
        logger.info("Fetching consultations for balai {}", balaiId);

        // Validate balai exists
        if (!balaiRepository.existsById(balaiId)) {
            throw new DomainException(ErrorCode.BALAI_NOT_FOUND);
        }

        List<OfflineConsultation> consultations = consultationRepository
                .findByBalaiIdOrderByConsultationDateDescConsultationTimeDesc(balaiId);

        logger.info("Found {} consultations for balai", consultations.size());

        return consultations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Generate time slots berdasarkan interval
     */
    private List<String> generateTimeSlots(int intervalMinutes) {
        List<String> slots = new ArrayList<>();
        LocalTime currentTime = BUSINESS_HOURS_START;

        while (!currentTime.isAfter(BUSINESS_HOURS_END)) {
            // Don't add slot if it equals or exceeds end time
            if (currentTime.isBefore(BUSINESS_HOURS_END) || currentTime.equals(BUSINESS_HOURS_END)) {
                slots.add(currentTime.toString());
            }
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }

        return slots;
    }

    /**
     * Calculate total slots per day
     */
    private int calculateTotalSlots(int intervalMinutes) {
        int totalMinutes = BUSINESS_HOURS_END.toSecondOfDay() - BUSINESS_HOURS_START.toSecondOfDay();
        return (totalMinutes / 60) / (intervalMinutes / 60);
    }

    /**
     * Calculate status summary dari list consultations
     */
    private ConsultationStatusSummary calculateStatusSummary(List<OfflineConsultation> consultations) {
        long pending = consultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.PENDING)
                .count();
        long confirmed = consultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.CONFIRMED)
                .count();
        long completed = consultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.COMPLETED)
                .count();
        long cancelled = consultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.CANCELLED)
                .count();

        return ConsultationStatusSummary.builder()
                .pending(pending)
                .confirmed(confirmed)
                .completed(completed)
                .cancelled(cancelled)
                .build();
    }

    private void validateStatusTransition(ConsultationStatus currentStatus, ConsultationStatus newStatus) {
        // Cannot change status of cancelled consultation
        if (currentStatus == ConsultationStatus.CANCELLED) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Cannot change status of a cancelled consultation");
        }

        // Cannot change status of completed consultation
        if (currentStatus == ConsultationStatus.COMPLETED) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Cannot change status of a completed consultation");
        }

        // Pending can go to: confirmed, cancelled
        if (currentStatus == ConsultationStatus.PENDING &&
            newStatus != ConsultationStatus.CONFIRMED &&
            newStatus != ConsultationStatus.CANCELLED) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Pending consultation can only be confirmed or cancelled");
        }

        // Confirmed can go to: completed, cancelled
        if (currentStatus == ConsultationStatus.CONFIRMED &&
            newStatus != ConsultationStatus.COMPLETED &&
            newStatus != ConsultationStatus.CANCELLED) {
            throw new DomainException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Confirmed consultation can only be completed or cancelled");
        }
    }

    private OfflineConsultationResponse mapToResponse(OfflineConsultation consultation) {
        return OfflineConsultationResponse.builder()
                .id(consultation.getId())
                .user(UserSummary.builder()
                        .id(consultation.getUser().getId())
                        .name(consultation.getUser().getName())
                        .build())
                .balai(BalaiSummary.builder()
                        .id(consultation.getBalai().getId())
                        .name(consultation.getBalai().getName())
                        .build())
                .consultationDate(consultation.getConsultationDate())
                .consultationTime(consultation.getConsultationTime())
                .status(consultation.getStatus().name().toLowerCase())
                .createdAt(consultation.getCreatedAt())
                .updatedAt(consultation.getUpdatedAt())
                .build();
    }
}

