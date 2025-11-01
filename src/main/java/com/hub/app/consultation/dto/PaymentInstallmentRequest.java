package com.hub.app.consultation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentInstallmentRequest {
    private BigDecimal percentage; // e.g., 30.0 for 30%
    private LocalDate dueDate;     // Custom due date from frontend
}