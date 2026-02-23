package com.hub.app.consultationoffline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO untuk calendar view (per bulan)
 * Menampilkan tanggal-tanggal yang ada booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedDatesCalendarResponse {
    
    /**
     * ID Balai
     */
    private UUID balaiId;
    
    /**
     * Nama Balai
     */
    private String balaiName;
    
    /**
     * Bulan (1-12)
     */
    private int month;
    
    /**
     * Tahun
     */
    private int year;
    
    /**
     * List tanggal dengan info booking
     */
    private List<BookedDateResponse> dates;
    
    /**
     * Total hari dalam bulan ini yang ada booking
     */
    private int totalDaysWithBookings;
    
    /**
     * Total hari dalam bulan ini yang fully booked
     */
    private int totalFullyBookedDays;
}
