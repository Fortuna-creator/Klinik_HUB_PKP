package com.hub.app.project.dto;

import com.hub.app.project.model.ProjectType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProjectRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @NotBlank(message = "Location detail is required")
    @Size(max = 255, message = "Location detail must not exceed 255 characters")
    private String locationDetail;

    @NotNull(message = "Type is required")
    private ProjectType type;

    @NotNull(message = "Land area is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Land area must be greater than 0")
    private BigDecimal landArea;

    private BigDecimal income;
}