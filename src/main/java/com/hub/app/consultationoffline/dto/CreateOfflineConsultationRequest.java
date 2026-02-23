package com.hub.app.consultationoffline.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOfflineConsultationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Balai ID is required")
    private String balaiId;

    @NotNull(message = "Consultation date is required")
    private LocalDate consultationDate;

    @NotNull(message = "Consultation time is required")
    private LocalTime consultationTime;
}

