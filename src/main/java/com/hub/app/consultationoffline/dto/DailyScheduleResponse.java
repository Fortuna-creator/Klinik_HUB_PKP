package com.hub.app.consultationoffline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO untuk jadwal harian balai
 * Menampilkan semua consultation di tanggal tertentu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyScheduleResponse {
    
    /**
     * ID Balai
     */
    private UUID balaiId;
    
    /**
     * Nama Balai
     */
    private String balaiName;
    
    /**
     * Tanggal
     */
    private LocalDate date;
    
    /**
     * List consultation di hari ini (sorted by time)
     */
    private List<OfflineConsultationResponse> consultations;
    
    /**
     * Total consultation hari ini
     */
    private int totalConsultations;
    
    /**
     * Jumlah slot yang masih available
     */
    private int availableSlots;
    
    /**
     * Jam operasional
     */
    private BusinessHours businessHours;
}
