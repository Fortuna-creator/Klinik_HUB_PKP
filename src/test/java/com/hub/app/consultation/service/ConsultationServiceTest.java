package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.ConsultationResponse;
import com.hub.app.consultation.dto.CreateConsultationRequest;
import com.hub.app.consultation.model.*;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.HomeOwner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @InjectMocks
    private ConsultationService consultationService;

    @Test
    void acceptConsultation_ShouldUpdateStatus_WhenValidTransition() {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        Long consultantId = 1L;

        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setId(2L);
        homeOwner.setName("John Doe");

        Consultant consultant = new Consultant();
        consultant.setId(consultantId);
        consultant.setName("Jane Smith");

        Consultation consultation = new Consultation();
        consultation.setId(consultationId);
        consultation.setHomeOwner(homeOwner);
        consultation.setConsultant(consultant);
        consultation.setStatus(ConsultationStatus.MENUNGGU_KONFIRMASI_KONSULTAN);

        when(consultationRepository.findByIdAndConsultantId(consultationId, consultantId))
                .thenReturn(Optional.of(consultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(consultation);

        // Act
        ConsultationResponse response = consultationService.acceptConsultation(consultationId, consultantId);

        // Assert
        assertNotNull(response);
        assertEquals(consultationId, response.getId());
        assertEquals(ConsultationStatus.MENUNGGU_JADWAL_SURVEY, consultation.getStatus());
        verify(consultationRepository).save(consultation);
    }

    @Test
    void acceptConsultation_ShouldThrowException_WhenInvalidStatus() {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        Long consultantId = 1L;

        Consultation consultation = new Consultation();
        consultation.setStatus(ConsultationStatus.SELESAI); // Invalid status for acceptance

        when(consultationRepository.findByIdAndConsultantId(consultationId, consultantId))
                .thenReturn(Optional.of(consultation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                consultationService.acceptConsultation(consultationId, consultantId));
        assertEquals("Invalid status transition", exception.getMessage());
    }

    @Test
    void acceptConsultation_ShouldThrowException_WhenConsultationNotFound() {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        Long consultantId = 1L;

        when(consultationRepository.findByIdAndConsultantId(consultationId, consultantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                consultationService.acceptConsultation(consultationId, consultantId));
        assertEquals("Consultation not found", exception.getMessage());
    }

    @Test
    void rejectConsultation_ShouldUpdateStatus_WhenValidTransition() {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        Long consultantId = 1L;

        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setId(2L);
        homeOwner.setName("John Doe");

        Consultant consultant = new Consultant();
        consultant.setId(consultantId);
        consultant.setName("Jane Smith");

        Consultation consultation = new Consultation();
        consultation.setId(consultationId);
        consultation.setHomeOwner(homeOwner);
        consultation.setConsultant(consultant);
        consultation.setStatus(ConsultationStatus.MENUNGGU_KONFIRMASI_KONSULTAN);

        when(consultationRepository.findByIdAndConsultantId(consultationId, consultantId))
                .thenReturn(Optional.of(consultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(consultation);

        // Act
        ConsultationResponse response = consultationService.rejectConsultation(consultationId, consultantId);

        // Assert
        assertNotNull(response);
        assertEquals(consultationId, response.getId());
        assertEquals(ConsultationStatus.SELESAI, consultation.getStatus());
        verify(consultationRepository).save(consultation);
    }

    @Test
    void rejectConsultation_ShouldThrowException_WhenInvalidStatus() {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        Long consultantId = 1L;

        Consultation consultation = new Consultation();
        consultation.setStatus(ConsultationStatus.SELESAI); // Invalid status for rejection

        when(consultationRepository.findByIdAndConsultantId(consultationId, consultantId))
                .thenReturn(Optional.of(consultation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                consultationService.rejectConsultation(consultationId, consultantId));
        assertEquals("Invalid status transition", exception.getMessage());
    }

    @Test
    void mapToResponse_ShouldMapConsultationToResponse() {
        // Arrange
        UUID consultationId = UUID.randomUUID();

        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setId(2L);
        homeOwner.setName("John Doe");

        Consultant consultant = new Consultant();
        consultant.setId(1L);
        consultant.setName("Jane Smith");

        Design design = new Design();
        design.setId(UUID.randomUUID());
        design.setName("Modern House Design");

        Consultation consultation = new Consultation();
        consultation.setId(consultationId);
        consultation.setHomeOwner(homeOwner);
        consultation.setConsultant(consultant);
        consultation.setDesign(design);
        consultation.setConsultationType(ConsultationType.GRATIS);
        consultation.setChannel(ConsultationChannel.CHAT);
        consultation.setStatus(ConsultationStatus.MENUNGGU_JADWAL_SURVEY);

        // Act
        ConsultationResponse response = consultationService.mapToResponse(consultation);

        // Assert
        assertNotNull(response);
        assertEquals(consultationId, response.getId());
        assertEquals(2L, response.getHomeOwnerId());
        assertEquals("John Doe", response.getHomeOwnerName());
        assertEquals(1L, response.getConsultantId());
        assertEquals("Jane Smith", response.getConsultantName());
        assertEquals(design.getId(), response.getDesignId());
        assertEquals("Modern House Design", response.getDesignName());
        assertEquals(ConsultationType.GRATIS, response.getConsultationType());
        assertEquals(ConsultationChannel.CHAT, response.getChannel());
        assertEquals(ConsultationStatus.MENUNGGU_JADWAL_SURVEY, response.getStatus());
    }
}