package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.CreatePaymentRequest;
import com.hub.app.consultation.dto.PaymentResponse;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.Payment;
import com.hub.app.consultation.model.PaymentStatus;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.consultation.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ConsultationRepository consultationRepository;

    public PaymentResponse createPayment(UUID consultationId, CreatePaymentRequest request, Long homeOwnerId) {
        Consultation consultation = consultationRepository.findByIdAndHomeOwnerId(consultationId, homeOwnerId)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.CONSULTATION_NOT_FOUND));

        Payment payment = new Payment();
        payment.setConsultation(consultation);
        payment.setTermin(request.getTermin());
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.MENUNGGU);

        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
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