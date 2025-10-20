package com.hub.app.consultation.dto;

import com.hub.app.consultation.model.ConsultationChannel;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.model.ConsultationType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ConsultationResponse {
    private UUID id;
    private Long homeOwnerId;
    private String homeOwnerName;
    private Long consultantId;
    private String consultantName;
    private UUID designId;
    private String designName;
    private ConsultationType consultationType;
    private ConsultationChannel channel;
    private ConsultationStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
}