package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.PaymentInstallment;
import com.hub.app.consultation.dto.PaymentInstallmentRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class PaymentInstallmentCalculator {

    /**
     * Calculate payment installments with custom percentages from frontend
     * @param totalAmount Total contract value
     * @param installmentRequests List of installment requests with custom percentages and due dates. If null/empty, creates single payment after signing
     * @return List of PaymentInstallment
     */
    public List<PaymentInstallment> calculateInstallments(BigDecimal totalAmount, List<PaymentInstallmentRequest> installmentRequests) {
        if (installmentRequests == null || installmentRequests.isEmpty()) {
            // Create single payment installment after contract signing
            return createSinglePaymentInstallment(totalAmount);
        }

        if (installmentRequests.size() > 3) {
            throw new IllegalArgumentException("Maximum 3 installments allowed");
        }

        // Validate total percentage equals 100%
        BigDecimal totalPercentage = installmentRequests.stream()
                .map(PaymentInstallmentRequest::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("Total installment percentages must equal 100%");
        }

        List<PaymentInstallment> installments = new ArrayList<>();

        for (int i = 0; i < installmentRequests.size(); i++) {
            PaymentInstallmentRequest request = installmentRequests.get(i);
            BigDecimal amount = totalAmount.multiply(request.getPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // Adjust last installment to ensure total equals contract value
            if (i == installmentRequests.size() - 1) {
                BigDecimal calculatedTotal = installments.stream()
                        .map(PaymentInstallment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                amount = totalAmount.subtract(calculatedTotal);
            }

            installments.add(new PaymentInstallment(i + 1, amount, request.getDueDate()));
        }

        return installments;
    }

    /**
     * Create a single payment installment after contract signing
     * @param totalAmount Total contract value
     * @return List containing single PaymentInstallment
     */
    private List<PaymentInstallment> createSinglePaymentInstallment(BigDecimal totalAmount) {
        List<PaymentInstallment> installments = new ArrayList<>();
        // Payment due immediately after contract signing (same day)
        installments.add(new PaymentInstallment(1, totalAmount, LocalDate.now()));
        return installments;
    }

    /**
     * Legacy method for backward compatibility - calculate with fixed distribution: 30% - 40% - 30%
     * @param totalAmount Total contract value
     * @param numberOfInstallments Number of installments (1-3)
     * @return List of PaymentInstallment
     */
    @Deprecated
    public List<PaymentInstallment> calculateInstallments(BigDecimal totalAmount, int numberOfInstallments) {
        if (numberOfInstallments < 1 || numberOfInstallments > 3) {
            throw new IllegalArgumentException("Number of installments must be between 1 and 3");
        }

        List<PaymentInstallment> installments = new ArrayList<>();
        LocalDate baseDate = LocalDate.now();

        switch (numberOfInstallments) {
            case 1:
                // Single payment
                installments.add(new PaymentInstallment(1, totalAmount, baseDate.plusDays(7)));
                break;

            case 2:
                // 50% - 50% distribution for 2 installments
                BigDecimal halfAmount = totalAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                installments.add(new PaymentInstallment(1, halfAmount, baseDate.plusDays(7)));
                installments.add(new PaymentInstallment(2, totalAmount.subtract(halfAmount), baseDate.plusDays(30)));
                break;

            case 3:
                // 30% - 40% - 30% distribution
                BigDecimal firstAmount = totalAmount.multiply(BigDecimal.valueOf(0.3)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal secondAmount = totalAmount.multiply(BigDecimal.valueOf(0.4)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal thirdAmount = totalAmount.subtract(firstAmount).subtract(secondAmount);

                installments.add(new PaymentInstallment(1, firstAmount, baseDate.plusDays(7)));
                installments.add(new PaymentInstallment(2, secondAmount, baseDate.plusDays(30)));
                installments.add(new PaymentInstallment(3, thirdAmount, baseDate.plusDays(60)));
                break;
        }

        return installments;
    }
}