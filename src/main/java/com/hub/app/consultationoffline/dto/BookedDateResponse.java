package com.hub.app.consultationoffline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO untuk informasi tanggal yang ada booking
 * Digunakan untuk: Calendar month view - highlight dates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedDateResponse {
    
    /**
     * Tanggal
     */
    private LocalDate date;
    
    /**
     * Jumlah total booking di tanggal ini
     */
    private int totalBookings;
    
    /**
     * Jumlah slot yang masih available
     */
    private int availableSlots;
    
    /**
     * Jumlah total slot per hari (default: 9 slots dari 08:00-17:00)
     */
    private int totalSlots;
    
    /**
     * Apakah tanggal ini fully booked (semua slot sudah terisi)
     */
    private boolean isFullyBooked;
    
    /**
     * Percentage booking (untuk visualisasi)
     * Contoh: 75.0 berarti 75% slot sudah terbooked
     */
    private double bookingPercentage;
}
