package com.hub.app.consultation.dto;

import com.hub.app.consultation.model.ContractStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ContractResponse {
    private UUID id;
    private UUID consultationId;
    private String pdfUrl;
    private Boolean digitalSignature;
    private ContractStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}