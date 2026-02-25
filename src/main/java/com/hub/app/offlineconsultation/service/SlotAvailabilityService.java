package com.hub.app.offlineconsultation.service;

import com.hub.app.offlineconsultation.dto.response.AvailableSlotResponse;
import com.hub.app.offlineconsultation.entity.ConsultationStatus;
import com.hub.app.offlineconsultation.entity.OfflineConsultationEntity;
import com.hub.app.offlineconsultation.repository.OcConsultationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Calculates available time slots for a given Balai (office) on a given date.
 *
 * <h3>How it works:</h3>
 * <p>Each Balai office has a fixed set of one-hour time slots during working hours
 * (defaulting to 08.00 – 16.00). Each slot has a maximum capacity of bookings it
 * can accept. This service queries the database for all <em>active</em> (non-cancelled)
 * bookings on that date, counts how many exist per slot, and returns a list indicating
 * whether each slot is still available.</p>
 *
 * <p>The frontend calls {@code GET /api/balai/{balaiId}/available-slots?date=YYYY-MM-DD}
 * and receives this list so users can pick an open slot.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlotAvailabilityService {

    private final OcConsultationRepository consultationRepository;

    /**
     * Default maximum number of concurrent bookings allowed per time slot.
     * In a production system, this could be stored per-Balai in the database.
     */
    private static final int DEFAULT_MAX_CAPACITY = 5;

    /**
     * The fixed list of one-hour time slots available for booking.
     * Format matches the API contract: "HH.MM - HH.MM".
     */
    private static final List<String> DEFAULT_TIME_SLOTS = Arrays.asList(
            "08.00 - 09.00",
            "09.00 - 10.00",
            "10.00 - 11.00",
            "11.00 - 12.00",
            "13.00 - 14.00",
            "14.00 - 15.00",
            "15.00 - 16.00"
    );

    /**
     * Returns the availability of every time slot for a specific Balai on a specific date.
     *
     * <h4>Steps:</h4>
     * <ol>
     *   <li>Fetch all <em>active</em> consultations for that Balai + date
     *       (active = any status except DIBATALKAN/cancelled).</li>
     *   <li>Group them by their {@code timeSlot} string and count how many bookings
     *       exist in each slot.</li>
     *   <li>For each of the default time slots, build an {@link AvailableSlotResponse}
     *       showing the booked count, max capacity, and whether the slot is still open.</li>
     * </ol>
     *
     * @param balaiId the UUID of the Balai office
     * @param date    the date to check
     * @return a list of slot availability objects, one per time slot
     */
    public List<AvailableSlotResponse> getAvailableSlots(UUID balaiId, LocalDate date) {

        // Step 1: Query all active (non-cancelled) bookings for this balai + date
        List<OfflineConsultationEntity> activeBookings =
                consultationRepository.findActiveByBalaiAndDate(
                        balaiId, date, ConsultationStatus.DIBATALKAN);

        // Step 2: Count bookings per time slot
        //   e.g. { "09.00 - 10.00" -> 3, "10.00 - 11.00" -> 1 }
        Map<String, Long> bookedCountPerSlot = activeBookings.stream()
                .collect(Collectors.groupingBy(
                        OfflineConsultationEntity::getTimeSlot,
                        Collectors.counting()));

        // Step 3: Build the response list
        List<AvailableSlotResponse> result = new ArrayList<>();

        for (String slot : DEFAULT_TIME_SLOTS) {
            int bookedCount = bookedCountPerSlot.getOrDefault(slot, 0L).intValue();
            boolean isAvailable = bookedCount < DEFAULT_MAX_CAPACITY;

            result.add(AvailableSlotResponse.builder()
                    .timeSlot(slot)
                    .isAvailable(isAvailable)
                    .bookedCount(bookedCount)
                    .maxCapacity(DEFAULT_MAX_CAPACITY)
                    .build());
        }

        return result;
    }

    /**
     * Quick check: is a specific time slot still available for booking?
     * Used internally by {@link OfflineConsultationService} during the booking flow.
     *
     * @return {@code true} if there is still capacity in that slot
     */
    public boolean isSlotAvailable(UUID balaiId, LocalDate date, String timeSlot) {
        long currentCount = consultationRepository.countActiveByBalaiAndDateAndSlot(
                balaiId, date, timeSlot, ConsultationStatus.DIBATALKAN);
        return currentCount < DEFAULT_MAX_CAPACITY;
    }
}
