package com.hub.app.consultationoffline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO untuk available time slots di tanggal tertentu
 * Digunakan untuk: Calendar time picker
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotsResponse {
    
    /**
     * Tanggal yang dicek
     */
    private LocalDate date;
    
    /**
     * ID Balai
     */
    private UUID balaiId;
    
    /**
     * Nama Balai
     */
    private String balaiName;
    
    /**
     * Jam operasional
     */
    private BusinessHours businessHours;
    
    /**
     * List waktu yang masih tersedia untuk booking
     * Format: ["08:00:00", "09:00:00", "11:00:00"]
     */
    private List<String> availableSlots;
    
    /**
     * List waktu yang sudah di-book
     * Format: ["10:00:00", "13:00:00", "15:00:00"]
     */
    private List<String> bookedSlots;
    
    /**
     * Total slot available
     */
    private int totalAvailableSlots;
    
    /**
     * Total slot yang sudah di-book
     */
    private int totalBookedSlots;
}
