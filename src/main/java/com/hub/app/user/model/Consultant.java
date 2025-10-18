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
}