package com.hub.app.consultation.dto;

import com.hub.app.consultation.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID consultationId;
    private Integer termin;
    private BigDecimal amount;
    private String method;
    private PaymentStatus status;
    private String paymentGatewayRef;
    private Instant createdAt;
    private Instant updatedAt;
}