package com.hub.app.offlineconsultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response item for GET /api/balai/{balaiId}/available-slots.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableSlotResponse {

    private String timeSlot;        // "09.00 - 10.00"
    private boolean isAvailable;
    private int bookedCount;
    private int maxCapacity;
}
