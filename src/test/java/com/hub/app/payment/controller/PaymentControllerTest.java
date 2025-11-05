package com.hub.app.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import com.hub.app.payment.dto.CreatePaymentRequest;
import com.hub.app.payment.dto.PaymentResponse;
import com.hub.app.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ConsultationJwtService jwtService;

    @Mock
    private ApiErrorBuilder apiErrorBuilder;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper;
    private UUID consultationId;
    private CreatePaymentRequest createPaymentRequest;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();

        consultationId = UUID.randomUUID();
        createPaymentRequest = new CreatePaymentRequest();
        createPaymentRequest.setAmount(BigDecimal.valueOf(1000));
        createPaymentRequest.setMethod("WALLET");
        createPaymentRequest.setReferenceId("REF123");

        paymentResponse = PaymentResponse.builder()
                .id(UUID.randomUUID())
                .consultationId(consultationId)
                .amount(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    void createPayment_ShouldReturnCreatedPayment() throws Exception {
        when(jwtService.extractUserId("Bearer token")).thenReturn(1L);
        when(paymentService.createPayment(eq(consultationId), any(CreatePaymentRequest.class), eq(1L)))
                .thenReturn(paymentResponse);

        mockMvc.perform(post("/api/payments")
                .param("consultationId", consultationId.toString())
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPaymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(paymentResponse.getId().toString()))
                .andExpect(jsonPath("$.consultationId").value(consultationId.toString()));

        verify(paymentService).createPayment(eq(consultationId), any(CreatePaymentRequest.class), eq(1L));
    }

    @Test
    void createPayment_ShouldHandleException() throws Exception {
        when(jwtService.extractUserId("Bearer token")).thenReturn(1L);
        when(paymentService.createPayment(eq(consultationId), any(CreatePaymentRequest.class), eq(1L)))
                .thenThrow(new RuntimeException("Test error"));
        when(apiErrorBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error"))
                .thenReturn(null);

        mockMvc.perform(post("/api/payments")
                .param("consultationId", consultationId.toString())
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPaymentRequest)))
                .andExpect(status().isInternalServerError());

        verify(apiErrorBuilder).error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error");
    }

    @Test
    void getPayments_ShouldReturnPaymentList() throws Exception {
        List<PaymentResponse> payments = List.of(paymentResponse);

        when(jwtService.extractUserId("Bearer token")).thenReturn(1L);
        when(paymentService.getPaymentsByConsultation(consultationId, 1L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments")
                .param("consultationId", consultationId.toString())
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(paymentResponse.getId().toString()));

        verify(paymentService).getPaymentsByConsultation(consultationId, 1L);
    }

    @Test
    void getPayments_ShouldHandleException() throws Exception {
        when(jwtService.extractUserId("Bearer token")).thenReturn(1L);
        when(paymentService.getPaymentsByConsultation(consultationId, 1L))
                .thenThrow(new RuntimeException("Test error"));
        when(apiErrorBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error"))
                .thenReturn(null);

        mockMvc.perform(get("/api/payments")
                .param("consultationId", consultationId.toString())
                .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError());

        verify(apiErrorBuilder).error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error");
    }

    @Test
    void requestPayment_ShouldReturnSuccessMessage() throws Exception {
        when(jwtService.extractUserId("Bearer token")).thenReturn(2L);
        doNothing().when(paymentService).requestPayment(consultationId, 2L);

        mockMvc.perform(post("/api/payments/request")
                .param("consultationId", consultationId.toString())
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment request sent to homeowner"));

        verify(paymentService).requestPayment(consultationId, 2L);
    }

    @Test
    void requestPayment_ShouldHandleException() throws Exception {
        when(jwtService.extractUserId("Bearer token")).thenReturn(2L);
        doThrow(new RuntimeException("Test error")).when(paymentService).requestPayment(consultationId, 2L);
        when(apiErrorBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error"))
                .thenReturn(null);

        mockMvc.perform(post("/api/payments/request")
                .param("consultationId", consultationId.toString())
                .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError());

        verify(apiErrorBuilder).error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error");
    }

    @Test
    void approvePayment_ShouldReturnApprovedPayment() throws Exception {
        UUID paymentId = UUID.randomUUID();

        when(jwtService.extractUserId("Bearer token")).thenReturn(1L);
        when(paymentService.approvePayment(paymentId, 1L)).thenReturn(paymentResponse);

        mockMvc.perform(post("/api/payments/{paymentId}/approve", paymentId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentResponse.getId().toString()));

        verify(paymentService).approvePayment(paymentId, 1L);
    }

    @Test
    void approvePayment_ShouldHandleException() throws Exception {
        UUID paymentId = UUID.randomUUID();

        when(jwtService.extractUserId("Bearer token")).thenReturn(1L);
        when(paymentService.approvePayment(paymentId, 1L)).thenThrow(new RuntimeException("Test error"));
        when(apiErrorBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error"))
                .thenReturn(null);

        mockMvc.perform(post("/api/payments/{paymentId}/approve", paymentId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError());

        verify(apiErrorBuilder).error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error");
    }

    @Test
    void releasePayment_ShouldReturnReleasedPayment() throws Exception {
        UUID paymentId = UUID.randomUUID();

        when(jwtService.extractUserId("Bearer token")).thenReturn(2L);
        when(paymentService.releasePayment(paymentId, 2L)).thenReturn(paymentResponse);

        mockMvc.perform(post("/api/payments/{paymentId}/release", paymentId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentResponse.getId().toString()));

        verify(paymentService).releasePayment(paymentId, 2L);
    }

    @Test
    void releasePayment_ShouldHandleException() throws Exception {
        UUID paymentId = UUID.randomUUID();

        when(jwtService.extractUserId("Bearer token")).thenReturn(2L);
        when(paymentService.releasePayment(paymentId, 2L)).thenThrow(new RuntimeException("Test error"));
        when(apiErrorBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error"))
                .thenReturn(null);

        mockMvc.perform(post("/api/payments/{paymentId}/release", paymentId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError());

        verify(apiErrorBuilder).error(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR.getCode(), "Test error");
    }
}