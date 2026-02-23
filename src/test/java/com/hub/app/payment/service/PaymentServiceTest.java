package com.hub.app.payment.service;

import com.hub.app.consultation.event.ConsultationEvent;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.user.model.HomeOwner;
import com.hub.app.user.model.Consultant;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.payment.dto.CreatePaymentRequest;
import com.hub.app.payment.dto.PaymentResponse;
import com.hub.app.payment.model.Payment;
import com.hub.app.payment.model.PaymentStatus;
import com.hub.app.payment.model.PaymentType;
import com.hub.app.payment.repository.PaymentRepository;
import com.hub.app.wallet.service.WalletService;
import com.hub.app.escrow.model.EscrowAccount;
import com.hub.app.escrow.service.EscrowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private WalletService walletService;

    @Mock
    private EscrowService escrowService;

    @InjectMocks
    private PaymentService paymentService;

    private Consultation testConsultation;
    private Payment testPayment;
    private CreatePaymentRequest testRequest;
    private UUID consultationId;

    @BeforeEach
    void setUp() {
        consultationId = UUID.randomUUID();

        testConsultation = new Consultation();
        testConsultation.setId(consultationId);
        testConsultation.setStatus(ConsultationStatus.AKTIF);

        testPayment = new Payment();
        testPayment.setId(UUID.randomUUID());
        testPayment.setConsultation(testConsultation);
        testPayment.setType(PaymentType.KONSULTASI);
        testPayment.setAmount(BigDecimal.valueOf(1000));
        testPayment.setStatus(PaymentStatus.ESCROW_PENDING);

        testRequest = new CreatePaymentRequest();
        testRequest.setType(PaymentType.KONSULTASI);
        testRequest.setAmount(BigDecimal.valueOf(1000));
        testRequest.setMethod("WALLET");
        testRequest.setReferenceId("REF123");
    }

    @Test
    void createPayment_ShouldCreatePaymentSuccessfully() {
        when(consultationRepository.findByIdAndHomeOwnerId(consultationId, 1L)).thenReturn(Optional.of(testConsultation));
        when(walletService.hasSufficientBalance(1L, BigDecimal.valueOf(1000))).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse result = paymentService.createPayment(consultationId, testRequest, 1L);

        assertNotNull(result);
        assertEquals(testPayment.getId(), result.getId());
        assertEquals(consultationId, result.getConsultationId());
        assertEquals(PaymentType.KONSULTASI, result.getType());
        assertEquals(BigDecimal.valueOf(1000), result.getAmount());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment captured = captor.getValue();
        assertEquals(PaymentType.KONSULTASI, captured.getType());
        assertEquals(PaymentStatus.ESCROW_PENDING, captured.getStatus());
    }

    @Test
    void createPayment_ShouldThrowExceptionWhenConsultationNotFound() {
        when(consultationRepository.findByIdAndHomeOwnerId(consultationId, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.createPayment(consultationId, testRequest, 1L));
    }

    @Test
    void createPayment_ShouldThrowExceptionWhenInsufficientBalance() {
        when(consultationRepository.findByIdAndHomeOwnerId(consultationId, 1L)).thenReturn(Optional.of(testConsultation));
        when(walletService.hasSufficientBalance(1L, BigDecimal.valueOf(1000))).thenReturn(false);

        assertThrows(RuntimeException.class, () -> paymentService.createPayment(consultationId, testRequest, 1L));
    }

    @Test
    void requestPayment_ShouldPublishEventWhenPendingPaymentsExist() {
        testConsultation.setStatus(ConsultationStatus.AKTIF);
        List<Payment> pendingPayments = List.of(testPayment);

        when(consultationRepository.findByIdAndConsultantId(consultationId, 2L)).thenReturn(Optional.of(testConsultation));
        when(paymentRepository.findByConsultationIdAndStatus(consultationId, PaymentStatus.MENUNGGU)).thenReturn(pendingPayments);

        paymentService.requestPayment(consultationId, 2L);

        ArgumentCaptor<ConsultationEvent> eventCaptor = ArgumentCaptor.forClass(ConsultationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ConsultationEvent event = eventCaptor.getValue();
        assertEquals("PAYMENT_REQUESTED", event.getEventType());
        assertNull(event.getPaymentId()); // PaymentService doesn't include paymentId in event
    }

    @Test
    void requestPayment_ShouldThrowExceptionWhenConsultationNotActive() {
        testConsultation.setStatus(ConsultationStatus.SELESAI);

        when(consultationRepository.findByIdAndConsultantId(consultationId, 2L)).thenReturn(Optional.of(testConsultation));

        assertThrows(RuntimeException.class, () -> paymentService.requestPayment(consultationId, 2L));
    }

    @Test
    void requestPayment_ShouldThrowExceptionWhenNoPendingPayments() {
        testConsultation.setStatus(ConsultationStatus.AKTIF);

        when(consultationRepository.findByIdAndConsultantId(consultationId, 2L)).thenReturn(Optional.of(testConsultation));
        when(paymentRepository.findByConsultationIdAndStatus(consultationId, PaymentStatus.MENUNGGU)).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> paymentService.requestPayment(consultationId, 2L));
    }

    @Test
    void getPaymentsByConsultation_ShouldReturnPaymentsForHomeOwner() {
        List<Payment> payments = List.of(testPayment);
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setId(1L);
        testConsultation.setHomeOwner(homeOwner);

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(testConsultation));
        when(paymentRepository.findByConsultationIdOrderByTerminAsc(consultationId)).thenReturn(payments);

        List<PaymentResponse> result = paymentService.getPaymentsByConsultation(consultationId, 1L);

        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
    }

    @Test
    void getPaymentsByConsultation_ShouldReturnPaymentsForConsultant() {
        List<Payment> payments = List.of(testPayment);
        Consultant consultant = new Consultant();
        consultant.setId(2L);
        testConsultation.setConsultant(consultant);
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setId(1L);
        testConsultation.setHomeOwner(homeOwner);

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(testConsultation));
        when(paymentRepository.findByConsultationIdOrderByTerminAsc(consultationId)).thenReturn(payments);

        List<PaymentResponse> result = paymentService.getPaymentsByConsultation(consultationId, 2L);

        assertEquals(1, result.size());
    }

    @Test
    void getPaymentsByConsultation_ShouldThrowExceptionForUnauthorizedUser() {
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setId(1L);
        testConsultation.setHomeOwner(homeOwner);
        Consultant consultant = new Consultant();
        consultant.setId(2L);
        testConsultation.setConsultant(consultant);

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(testConsultation));

        assertThrows(RuntimeException.class, () -> paymentService.getPaymentsByConsultation(consultationId, 3L));
    }

    @Test
    void createTypedPayment_ShouldCreatePaymentWithSpecificType() {
        testRequest.setType(PaymentType.PERIZINAN);
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setId(1L);
        testConsultation.setHomeOwner(homeOwner);

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(testConsultation));
        when(walletService.hasSufficientBalance(1L, BigDecimal.valueOf(1000))).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse result = paymentService.createTypedPayment(consultationId, PaymentType.PERIZINAN, testRequest, 1L);

        assertNotNull(result);
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment captured = captor.getValue();
        assertEquals(PaymentType.PERIZINAN, captured.getType());
    }

    @Test
    void getPaymentsByType_ShouldFilterPaymentsByType() {
        List<Payment> allPayments = List.of(testPayment);
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setId(1L);
        testConsultation.setHomeOwner(homeOwner);

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(testConsultation));
        when(paymentRepository.findByConsultationIdOrderByTerminAsc(consultationId)).thenReturn(allPayments);

        List<PaymentResponse> result = paymentService.getPaymentsByType(consultationId, PaymentType.KONSULTASI, 1L);

        assertEquals(1, result.size());
        assertEquals(PaymentType.KONSULTASI, result.get(0).getType());
    }

    @Test
    void approvePayment_ShouldUpdateStatusAndHoldFunds() {
        UUID paymentId = UUID.randomUUID();
        testPayment.setId(paymentId);
        testPayment.setStatus(PaymentStatus.MENUNGGU);
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setId(1L);
        testConsultation.setHomeOwner(homeOwner);

        EscrowAccount escrowAccount = new EscrowAccount();
        escrowAccount.setId(UUID.randomUUID());

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(escrowService.getEscrowByConsultationId(consultationId)).thenReturn(escrowAccount);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse result = paymentService.approvePayment(paymentId, 1L);

        assertNotNull(result);
        verify(escrowService).holdFunds(escrowAccount.getId(), BigDecimal.valueOf(1000));
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment captured = captor.getValue();
        assertEquals(PaymentStatus.ESCROW_HOLD, captured.getStatus());

        // Verify that consultation event is published for history tracking
        ArgumentCaptor<ConsultationEvent> eventCaptor = ArgumentCaptor.forClass(ConsultationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ConsultationEvent event = eventCaptor.getValue();
        assertEquals("CONSULTATION", event.getStep());
        assertEquals("STARTED", event.getState());
        assertEquals("HOMEOWNER", event.getActor());
        assertEquals(testConsultation, event.getConsultation());
        assertEquals(BigDecimal.valueOf(1000), event.getTotalPaymentAmount());

        // Verify that consultation status is updated to AKTIF
        ArgumentCaptor<Consultation> consultationCaptor = ArgumentCaptor.forClass(Consultation.class);
        verify(consultationRepository).save(consultationCaptor.capture());
        Consultation savedConsultation = consultationCaptor.getValue();
        assertEquals(ConsultationStatus.AKTIF, savedConsultation.getStatus());
        assertNotNull(savedConsultation.getStartedAt());
    }

    @Test
    void approvePayment_ShouldThrowExceptionForWrongStatus() {
        UUID paymentId = UUID.randomUUID();
        testPayment.setId(paymentId);
        testPayment.setStatus(PaymentStatus.ESCROW_HOLD);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        assertThrows(RuntimeException.class, () -> paymentService.approvePayment(paymentId, 1L));
    }

    @Test
    void releasePayment_ShouldUpdateStatusAndReleaseFunds() {
        UUID paymentId = UUID.randomUUID();
        testPayment.setId(paymentId);
        testPayment.setStatus(PaymentStatus.ESCROW_HOLD);
        testPayment.setEscrowAccountId(UUID.randomUUID());
        Consultant consultant = new Consultant();
        consultant.setId(2L);
        testConsultation.setConsultant(consultant);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse result = paymentService.releasePayment(paymentId, 2L);

        assertNotNull(result);
        verify(escrowService).releaseFunds(testPayment.getEscrowAccountId());
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment captured = captor.getValue();
        assertEquals(PaymentStatus.ESCROW_RELEASED, captured.getStatus());
    }

    @Test
    void releasePayment_ShouldThrowExceptionForWrongStatus() {
        UUID paymentId = UUID.randomUUID();
        testPayment.setId(paymentId);
        testPayment.setStatus(PaymentStatus.ESCROW_PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        assertThrows(RuntimeException.class, () -> paymentService.releasePayment(paymentId, 2L));
    }
}