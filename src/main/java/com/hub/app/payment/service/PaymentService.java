package com.hub.app.payment.service;

import com.hub.app.payment.dto.CreatePaymentRequest;
import com.hub.app.payment.dto.PaymentResponse;
import com.hub.app.consultation.event.ConsultationEvent;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.payment.model.Payment;
import com.hub.app.payment.model.PaymentStatus;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.payment.repository.PaymentRepository;
import com.hub.app.escrow.model.EscrowAccount;
import com.hub.app.escrow.service.EscrowService;
import com.hub.app.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ConsultationRepository consultationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WalletService walletService;
    private final EscrowService escrowService;

    @Transactional
    public PaymentResponse createPayment(UUID consultationId, CreatePaymentRequest request, Long homeOwnerId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, homeOwnerId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));

        // Check if home owner has sufficient balance
        if (!walletService.hasSufficientBalance(homeOwnerId, request.getAmount())) {
            throw new RuntimeException("Insufficient balance in wallet");
        }

        Payment payment = new Payment();
        payment.setConsultation(consultation);
        payment.setTermin(request.getTermin());
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.ESCROW_PENDING);

        payment = paymentRepository.save(payment);
        log.info("Created payment {} for consultation {} with escrow pending", payment.getId(), consultationId);

        return mapToResponse(payment);
    }

    @Transactional
    public void requestPayment(UUID consultationId, Long consultantId) {
        Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));

        if (consultation.getStatus() != ConsultationStatus.KONTRAK_DISETUJUI) {
            throw new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.INVALID_STATUS, "Payment can only be requested after contract is approved");
        }

        // Create escrow account for this consultation if not exists
        if (!escrowService.escrowExists(consultationId)) {
            // For now, use a default contract value - this should be from the actual contract
            BigDecimal contractValue = new BigDecimal("1000000"); // TODO: Get from contract
            EscrowAccount escrow = escrowService.createEscrow(consultation, contractValue);
            log.info("Created escrow account {} for consultation {}", escrow.getId(), consultationId);
        }

        // Publish event for payment request notification
        eventPublisher.publishEvent(new ConsultationEvent(
            this, consultation, "PAYMENT_REQUESTED", "PAYMENT", "REQUESTED", "CONSULTANT"
        ));
    }

    public List<PaymentResponse> getPaymentsByConsultation(UUID consultationId, Long userId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));

        // Check if user has access
        if (!consultation.getHomeOwner().getId().equals(userId) &&
            !consultation.getConsultant().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        List<Payment> payments = paymentRepository.findByConsultationIdOrderByTerminAsc(consultationId);
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse approvePayment(UUID paymentId, Long homeOwnerId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getConsultation().getHomeOwner().getId().equals(homeOwnerId)) {
            throw new RuntimeException("Access denied");
        }

        if (payment.getStatus() != PaymentStatus.ESCROW_PENDING) {
            throw new RuntimeException("Payment is not in pending escrow state");
        }

        // Get escrow account
        EscrowAccount escrow = escrowService.getEscrowByConsultationId(payment.getConsultation().getId());

        // Hold funds in escrow
        escrowService.holdFunds(escrow.getId(), payment.getAmount());

        // Update payment status
        payment.setStatus(PaymentStatus.ESCROW_HOLD);
        payment.setEscrowAccountId(escrow.getId());
        payment = paymentRepository.save(payment);

        log.info("Payment {} approved and funds held in escrow {}", paymentId, escrow.getId());
        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse releasePayment(UUID paymentId, Long consultantId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getConsultation().getConsultant().getId().equals(consultantId)) {
            throw new RuntimeException("Access denied");
        }

        if (payment.getStatus() != PaymentStatus.ESCROW_HOLD) {
            throw new RuntimeException("Payment is not in escrow hold state");
        }

        // Release funds from escrow
        escrowService.releaseFunds(payment.getEscrowAccountId());

        // Update payment status
        payment.setStatus(PaymentStatus.ESCROW_RELEASED);
        payment = paymentRepository.save(payment);

        log.info("Payment {} released from escrow to consultant", paymentId);
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .consultationId(payment.getConsultation().getId())
                .termin(payment.getTermin())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .paymentGatewayRef(payment.getPaymentGatewayRef())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}