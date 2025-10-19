package com.hub.app.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultantPortfolioResponse {
    private ConsultantInfo consultant;
    private List<PortfolioProjectInfo> portfolios;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsultantInfo {
        private Long id;
        private String name;
        private String email;
        private String specialization;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioProjectInfo {
        private Long id;
        private String projectName;
        private BigDecimal price;
        private String detailDescription;
        private List<String> imageUrls;
        private LocalDateTime createdAt;
    }
}