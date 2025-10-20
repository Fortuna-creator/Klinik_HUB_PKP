package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.Payment;
import com.hub.app.consultation.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByConsultationIdOrderByTerminAsc(UUID consultationId);

    List<Payment> findByConsultationIdAndStatus(UUID consultationId, PaymentStatus status);
}