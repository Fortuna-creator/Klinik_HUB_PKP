package com.hub.app.consultation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.consultation.dto.ConsultationResponse;
import com.hub.app.consultation.dto.CreateConsultationRequest;
import com.hub.app.consultation.model.ConsultationChannel;
import com.hub.app.consultation.model.ConsultationType;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.consultation.service.ConsultationService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ConsultationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsultationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConsultationService consultationService;

    @MockitoBean
    private ConsultationJwtService jwtService;

    @MockitoBean
    private ApiErrorBuilder apiErrorBuilder;

    @Test
    void createConsultation_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setConsultantId(1L);
        request.setConsultationType(ConsultationType.GRATIS);
        request.setChannel(ConsultationChannel.CHAT);

        ConsultationResponse response = ConsultationResponse.builder()
                .id(UUID.randomUUID())
                .homeOwnerId(2L)
                .homeOwnerName("John Doe")
                .consultantId(1L)
                .consultantName("Jane Smith")
                .consultationType(ConsultationType.GRATIS)
                .channel(ConsultationChannel.CHAT)
                .build();

        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        when(consultationService.createConsultation(2L, request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/consultations")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.homeOwnerId").value(2L))
                .andExpect(jsonPath("$.consultantId").value(1L));
    }

    @Test
    void createConsultation_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setConsultantId(1L);
        request.setConsultationType(ConsultationType.GRATIS);
        request.setChannel(ConsultationChannel.CHAT);

        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        when(consultationService.createConsultation(2L, request)).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/consultations")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getConsultations_ShouldReturnList_WhenHomeOwner() throws Exception {
        // Arrange
        List<ConsultationResponse> responses = Arrays.asList(
                ConsultationResponse.builder().id(UUID.randomUUID()).homeOwnerId(1L).build(),
                ConsultationResponse.builder().id(UUID.randomUUID()).homeOwnerId(1L).build()
        );

        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(jwtService.extractUserRole(anyString())).thenReturn("HOME_OWNER");
        when(consultationService.getConsultationsByHomeOwner(1L)).thenReturn(responses);

        // Act & Assert
        mockMvc.perform(get("/api/consultations")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getConsultations_ShouldReturnList_WhenConsultant() throws Exception {
        // Arrange
        List<ConsultationResponse> responses = Arrays.asList(
                ConsultationResponse.builder().id(UUID.randomUUID()).consultantId(1L).build()
        );

        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(jwtService.extractUserRole(anyString())).thenReturn("CONSULTANT");
        when(consultationService.getConsultationsByConsultant(1L)).thenReturn(responses);

        // Act & Assert
        mockMvc.perform(get("/api/consultations")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getConsultation_ShouldReturnConsultation_WhenValidId() throws Exception {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        ConsultationResponse response = ConsultationResponse.builder()
                .id(consultationId)
                .homeOwnerId(1L)
                .consultantId(2L)
                .build();

        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(jwtService.extractUserRole(anyString())).thenReturn("HOME_OWNER");
        when(consultationService.getConsultationById(consultationId, 1L, true)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/consultations/{id}", consultationId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(consultationId.toString()));
    }

    @Test
    void acceptConsultation_ShouldReturnUpdatedConsultation_WhenValid() throws Exception {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        ConsultationResponse response = ConsultationResponse.builder()
                .id(consultationId)
                .status(com.hub.app.consultation.model.ConsultationStatus.MENUNGGU_JADWAL_SURVEY)
                .build();

        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(consultationService.acceptConsultation(consultationId, 1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/consultations/{id}/accept", consultationId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MENUNGGU_JADWAL_SURVEY"));
    }

    @Test
    void rejectConsultation_ShouldReturnUpdatedConsultation_WhenValid() throws Exception {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        ConsultationResponse response = ConsultationResponse.builder()
                .id(consultationId)
                .status(com.hub.app.consultation.model.ConsultationStatus.SELESAI)
                .build();

        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(consultationService.rejectConsultation(consultationId, 1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/consultations/{id}/reject", consultationId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SELESAI"));
    }

    @Test
    void finalizeConsultation_ShouldReturnUpdatedConsultation_WhenValid() throws Exception {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        ConsultationResponse response = ConsultationResponse.builder()
                .id(consultationId)
                .status(com.hub.app.consultation.model.ConsultationStatus.FINAL)
                .build();

        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(consultationService.finalizeConsultation(consultationId, 1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/consultations/{id}/finalize", consultationId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINAL"));
    }
}