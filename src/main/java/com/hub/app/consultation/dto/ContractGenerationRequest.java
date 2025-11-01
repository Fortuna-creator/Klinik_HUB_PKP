package com.hub.app.consultation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ContractGenerationRequest {
    private BigDecimal contractValue;
    private List<PaymentInstallmentRequest> installments; // Custom installment percentages and due dates
    private String fileUrl;
}