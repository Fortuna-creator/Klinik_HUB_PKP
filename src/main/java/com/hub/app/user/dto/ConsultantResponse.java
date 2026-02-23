package com.hub.app.user.dto;

import com.hub.app.user.model.AvailabilityStatus;
import com.hub.app.user.model.ConsultantType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConsultantResponse {
    private String id;
    private String fullName;
    private String specialty;
    private BigDecimal rating;
    private BigDecimal distanceKm;
    private String avatarUrl;
    private String location;

    // New fields for enhanced consultant information
    private ConsultantType type; // PROFESSIONAL or STUDENT
    private String experienceLevel;
    private AvailabilityStatus availabilityStatus;
    private String contactInfo;

    // Pricing information (only for professionals)
    private BigDecimal hourlyRate;
    private BigDecimal packageCost;
}