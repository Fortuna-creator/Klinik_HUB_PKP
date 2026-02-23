package com.hub.app.consultationoffline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary statistik consultation berdasarkan status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationStatusSummary {
    
    /**
     * Jumlah pending
     */
    @Builder.Default
    private long pending = 0;
    
    /**
     * Jumlah confirmed
     */
    @Builder.Default
    private long confirmed = 0;
    
    /**
     * Jumlah completed
     */
    @Builder.Default
    private long completed = 0;
    
    /**
     * Jumlah cancelled
     */
    @Builder.Default
    private long cancelled = 0;
    
    /**
     * Total semua
     */
    public long getTotal() {
        return pending + confirmed + completed + cancelled;
    }
}
