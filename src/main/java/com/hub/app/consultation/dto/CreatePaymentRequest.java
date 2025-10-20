package com.hub.app.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    @NotNull
    private Integer termin;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String method;
}