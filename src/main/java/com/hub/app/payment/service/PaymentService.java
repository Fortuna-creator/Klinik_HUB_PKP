package com.hub.app.payment.service;

import com.hub.app.payment.dto.CreatePaymentRequest;
import com.hub.app.payment.dto.PaymentResponse;
import com.hub.app.consultation.event.ConsultationEvent;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.payment.model.Payment;
import com.hub.app.payment.model.PaymentStatus;
import com.hub.app.payment.model.PaymentType;
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

import java.util.List;

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
        payment.setType(request.getType() != null ? request.getType() : PaymentType.KONSULTASI);
        payment.setTermin(request.getTermin());
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setDescription(getPaymentDescription(payment.getType(), request.getTermin()));
        payment.setReferenceId(request.getReferenceId());
        payment.setStatus(PaymentStatus.ESCROW_PENDING);

        payment = paymentRepository.save(payment);
        log.info("Created payment {} for consultation {} with escrow pending", payment.getId(), consultationId);

        return mapToResponse(payment);
    }

    @Transactional
    public void requestPayment(UUID consultationId, Long consultantId) {
        Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));

        if (consultation.getStatus() != ConsultationStatus.AKTIF) {
            throw new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.INVALID_STATUS, "Payment can only be requested after consultation is active");
        }

        // Check if there are pending payments for this consultation
        List<Payment> pendingPayments = paymentRepository.findByConsultationIdAndStatus(consultationId, PaymentStatus.MENUNGGU);
        if (pendingPayments.isEmpty()) {
            throw new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.INVALID_STATUS, "No pending payments found for this consultation");
        }

        // Publish event for payment request notification
        eventPublisher.publishEvent(new ConsultationEvent(
            this, consultation, "PAYMENT_REQUESTED", "PAYMENT", "REQUESTED", "CONSULTANT"
        ));

        log.info("Payment request sent for consultation {} with {} pending payments", consultationId, pendingPayments.size());
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

    /**
     * Create payment for different types (permit, contractor, survey, etc.)
     */
    @Transactional
    public PaymentResponse createTypedPayment(UUID consultationId, PaymentType type, CreatePaymentRequest request, Long userId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));

        // Check if user has access (home owner for payments)
        if (!consultation.getHomeOwner().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // Check if home owner has sufficient balance
        if (!walletService.hasSufficientBalance(userId, request.getAmount())) {
            throw new RuntimeException("Insufficient balance in wallet");
        }

        Payment payment = new Payment();
        payment.setConsultation(consultation);
        payment.setType(type);
        payment.setTermin(request.getTermin());
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setDescription(getPaymentDescription(type, request.getTermin()));
        payment.setStatus(PaymentStatus.ESCROW_PENDING);
        payment.setReferenceId(request.getReferenceId()); // Can reference permit, contractor, etc.

        payment = paymentRepository.save(payment);
        log.info("Created {} payment {} for consultation {}", type, payment.getId(), consultationId);

        return mapToResponse(payment);
    }

    /**
     * Get payments by type for a consultation
     */
    public List<PaymentResponse> getPaymentsByType(UUID consultationId, PaymentType type, Long userId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));

        // Check if user has access
        if (!consultation.getHomeOwner().getId().equals(userId) &&
            !consultation.getConsultant().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        List<Payment> payments = paymentRepository.findByConsultationIdOrderByTerminAsc(consultationId).stream()
                .filter(p -> p.getType() == type)
                .collect(Collectors.toList());
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String getPaymentDescription(PaymentType type, Integer termin) {
        String baseDesc = switch (type) {
            case KONSULTASI -> "Pembayaran konsultasi desain";
            case PERIZINAN -> "Pembayaran biaya perizinan";
            case KONTRAKTOR -> "Pembayaran biaya kontraktor";
            case SURVEY -> "Pembayaran biaya survey";
            case MATERIAL -> "Pembayaran biaya material";
            case LAINNYA -> "Pembayaran biaya lainnya";
        };

        return termin != null ? baseDesc + " termin " + termin : baseDesc;
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
                .type(payment.getType())
                .termin(payment.getTermin())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .description(payment.getDescription())
                .status(payment.getStatus())
                .paymentGatewayRef(payment.getPaymentGatewayRef())
                .dueDate(payment.getDueDate())
                .referenceId(payment.getReferenceId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}