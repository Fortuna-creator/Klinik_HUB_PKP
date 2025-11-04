package com.hub.app.consultation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ContractGenerationRequest {
    private BigDecimal contractValue;
    private List<PaymentInstallmentRequest> installments; // Optional: Custom installment percentages and due dates. If null/empty, single payment after signing
    private String fileUrl;
}