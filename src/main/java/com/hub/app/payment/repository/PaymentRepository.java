package com.hub.app.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hub.app.payment.model.Payment;
import com.hub.app.payment.model.PaymentStatus;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByConsultationIdOrderByTerminAsc(UUID consultationId);

    List<Payment> findByConsultationIdAndStatus(UUID consultationId, PaymentStatus status);
}