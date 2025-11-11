package com.hub.app.consultationoffline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO untuk jam operasional Balai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessHours {
    
    /**
     * Jam buka
     * Default: "08:00:00"
     */
    @Builder.Default
    private String start = "08:00:00";
    
    /**
     * Jam tutup
     * Default: "17:00:00"
     */
    @Builder.Default
    private String end = "17:00:00";
    
    /**
     * Interval slot (dalam menit)
     * Default: 60 (1 jam per slot)
     */
    @Builder.Default
    private int slotIntervalMinutes = 60;
}
