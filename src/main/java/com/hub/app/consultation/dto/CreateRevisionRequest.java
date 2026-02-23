package com.hub.app.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRevisionRequest {
    @NotBlank
    private String notes;

    private String fileUrl; // Optional, can be uploaded separately
}