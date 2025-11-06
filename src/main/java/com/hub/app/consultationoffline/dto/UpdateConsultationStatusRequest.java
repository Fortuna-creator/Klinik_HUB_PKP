package com.hub.app.consultationoffline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateConsultationStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(pending|confirmed|completed|cancelled)$",
             message = "Status must be one of: pending, confirmed, completed, cancelled")
    private String status;
}

