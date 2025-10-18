package com.hub.app.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "consultants")
@Data @NoArgsConstructor @AllArgsConstructor
public class Consultant extends User {

    // Location fields for distance calculation
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    // Consultant specific fields
    private String specialty;
    private BigDecimal rating;
    private String location; // Address/location description

    // New fields for professional vs student differentiation
    @Enumerated(EnumType.STRING)
    private ConsultantType type; // PROFESSIONAL or STUDENT

    private String experienceLevel; // e.g., "Beginner", "Intermediate", "Expert"

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus; // AVAILABLE, BUSY, UNAVAILABLE

    private String contactInfo; // Additional contact information

    // Pricing fields (only for professionals)
    private BigDecimal hourlyRate;
    private BigDecimal packageCost;
}