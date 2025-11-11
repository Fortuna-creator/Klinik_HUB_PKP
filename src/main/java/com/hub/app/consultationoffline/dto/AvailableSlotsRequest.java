package com.hub.app.consultationoffline.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO untuk query available slots
 * Digunakan sebagai query parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotsRequest {
    
    /**
     * ID Balai (required)
     */
    @NotNull(message = "Balai ID is required")
    private UUID balaiId;
    
    /**
     * Tanggal yang ingin dicek (required)
     */
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    /**
     * Interval slot dalam menit (optional)
     * Default: 60 menit (1 jam per slot)
     */
    @Builder.Default
    private Integer slotIntervalMinutes = 60;
}
