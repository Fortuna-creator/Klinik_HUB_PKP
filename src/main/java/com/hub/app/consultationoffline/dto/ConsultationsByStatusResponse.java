package com.hub.app.consultationoffline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO untuk consultations yang di-group by status
 * Berguna untuk dashboard summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationsByStatusResponse {
    
    /**
     * Total semua consultation
     */
    private long total;
    
    /**
     * Jumlah consultation pending
     */
    private long pending;
    
    /**
     * Jumlah consultation confirmed
     */
    private long confirmed;
    
    /**
     * Jumlah consultation completed
     */
    private long completed;
    
    /**
     * Jumlah consultation cancelled
     */
    private long cancelled;
    
    /**
     * List consultation data (optional)
     */
    private List<OfflineConsultationResponse> consultations;
}
