package com.hub.app.user.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioProjectRequest {
    private String projectName;
    private BigDecimal price;
    private String detailDescription;
    private List<String> imageUrls;
}