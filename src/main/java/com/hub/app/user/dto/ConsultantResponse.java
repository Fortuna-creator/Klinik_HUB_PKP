package com.hub.app.user.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConsultantResponse {
    private String consultantId;
    private String fullName;
    private String specialty;
    private BigDecimal rating;
    private BigDecimal distanceKm;
    private String avatarUrl;
    private String location;
}