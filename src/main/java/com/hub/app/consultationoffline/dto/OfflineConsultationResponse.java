package com.hub.app.consultationoffline.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineConsultationResponse {

    private UUID id;

    private UserSummary user;

    private BalaiSummary balai;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate consultationDate;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime consultationTime;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant updatedAt;
}

