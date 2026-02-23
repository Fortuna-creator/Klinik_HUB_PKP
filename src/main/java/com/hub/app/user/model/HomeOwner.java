package com.hub.app.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "homeowners")
@Data @NoArgsConstructor @AllArgsConstructor
public class HomeOwner extends User {

    // HomeOwner specific fields
    private String propertyType; // Type of property they own
    private BigDecimal propertyValue; // Estimated property value
    private String projectNeeds; // What kind of projects they need help with

    // Verification fields exclusive to HomeOwner
    private String ktpNumber; // KTP number for verification
    private String ktpUrl;
    private String selfieUrl;

}