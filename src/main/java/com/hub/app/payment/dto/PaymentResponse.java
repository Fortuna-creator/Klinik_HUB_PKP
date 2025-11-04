package com.hub.app.payment.dto;

import com.hub.app.payment.model.PaymentStatus;
import com.hub.app.payment.model.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID consultationId;
    private PaymentType type;
    private Integer termin;
    private BigDecimal amount;
    private String method;
    private String description;
    private PaymentStatus status;
    private String paymentGatewayRef;
    private LocalDateTime dueDate;
    private String referenceId;
    private Instant createdAt;
    private Instant updatedAt;
}