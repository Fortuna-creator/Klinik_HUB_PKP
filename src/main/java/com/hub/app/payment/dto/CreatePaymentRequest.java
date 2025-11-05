package com.hub.app.payment.dto;

import com.hub.app.payment.model.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private PaymentType type; // Optional, defaults to KONSULTASI

    private Integer termin; // Optional for single payments

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String method;

    private String referenceId; // Reference to contract, permit, etc.
}