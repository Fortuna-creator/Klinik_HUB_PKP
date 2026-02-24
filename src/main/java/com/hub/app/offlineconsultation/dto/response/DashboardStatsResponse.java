package com.hub.app.offlineconsultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for GET /api/dashboard/stats.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private long todayTotal;
    private long todayPending;       // menunggu_konfirmasi
    private long todayConfirmed;     // terkonfirmasi
    private long todayCompleted;     // selesai
    private long todayRejected;      // dibatalkan
}
