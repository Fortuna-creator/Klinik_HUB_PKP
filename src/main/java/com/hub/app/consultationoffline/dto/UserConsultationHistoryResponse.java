package com.hub.app.consultationoffline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO untuk user consultation history
 * Terpisah antara upcoming dan past consultations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConsultationHistoryResponse {
    
    /**
     * User ID
     */
    private Long userId;
    
    /**
     * Nama user
     */
    private String userName;
    
    /**
     * Consultation yang akan datang (upcoming)
     */
    private List<OfflineConsultationResponse> upcomingConsultations;
    
    /**
     * Riwayat consultation yang sudah lewat
     */
    private List<OfflineConsultationResponse> pastConsultations;
    
    /**
     * Total upcoming consultations
     */
    private int totalUpcoming;
    
    /**
     * Total past consultations
     */
    private int totalPast;
    
    /**
     * Summary by status
     */
    private ConsultationStatusSummary statusSummary;
}
