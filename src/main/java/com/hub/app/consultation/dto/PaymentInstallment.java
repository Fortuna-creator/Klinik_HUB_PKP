package com.hub.app.consultation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PaymentInstallment {
    private int installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
}