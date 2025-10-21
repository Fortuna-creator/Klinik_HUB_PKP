package com.hub.app.integration.simbg.service;

import com.hub.app.integration.simbg.client.SimbgClient;
import com.hub.app.integration.simbg.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimbgIntegrationServiceTest {

    @Mock
    private SimbgClient simbgClient;

    @InjectMocks
    private SimbgIntegrationServiceImpl simbgIntegrationService;

    private SimbgRegistrationRequest registrationRequest;
    private SimbgRegistrationResponse registrationResponse;
    private SimbgStatusResponse statusResponse;
    private List<SimbgDocumentResponse> documentResponses;
    private SimbgUpdateRequest updateRequest;
    private SimbgUpdateResponse updateResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        registrationRequest = new SimbgRegistrationRequest();
        registrationRequest.setApplicantName("John Doe");
        registrationRequest.setApplicantIdNumber("123456789");
        registrationRequest.setPropertyAddress("Jl. Example No. 123");
        registrationRequest.setPrototypeType("NON_PROTOTYPE");
        registrationRequest.setExternalReference("EXT-001");

        registrationResponse = new SimbgRegistrationResponse();
        registrationResponse.setSimbgId("SIMBG-123");
        registrationResponse.setStatus("SUBMITTED");

        statusResponse = new SimbgStatusResponse();
        statusResponse.setSimbgId("SIMBG-123");
        statusResponse.setExternalReference("EXT-001");
        statusResponse.setStatus("APPROVED");

        SimbgDocumentResponse documentResponse = new SimbgDocumentResponse();
        documentResponse.setDocId("DOC-001");
        documentResponse.setType("PBG");
        documentResponse.setIssuedAt(LocalDate.now());
        documentResponse.setDownloadUrl("https://example.com/doc.pdf");
        documentResponse.setChecksum("abc123");
        documentResponses = List.of(documentResponse);

        updateRequest = new SimbgUpdateRequest();
        updateRequest.setComments("Updated documents");

        updateResponse = new SimbgUpdateResponse();
        updateResponse.setSimbgId("SIMBG-123");
        updateResponse.setStatus("UPDATED");
        updateResponse.setMessage("Application updated successfully");
    }

    @Test
    void registerApplication_ShouldReturnRegistrationResponse() {
        // Given
        when(simbgClient.registerApplication(any(SimbgRegistrationRequest.class)))
                .thenReturn(Mono.just(registrationResponse));

        // When
        SimbgRegistrationResponse result = simbgIntegrationService.registerApplication(registrationRequest);

        // Then
        assertNotNull(result);
        assertEquals("SIMBG-123", result.getSimbgId());
        assertEquals("SUBMITTED", result.getStatus());
    }

    @Test
    void trackStatus_ShouldReturnStatusResponse() {
        // Given
        String simbgId = "SIMBG-123";
        when(simbgClient.trackStatus(simbgId)).thenReturn(Mono.just(statusResponse));

        // When
        SimbgStatusResponse result = simbgIntegrationService.trackStatus(simbgId);

        // Then
        assertNotNull(result);
        assertEquals("SIMBG-123", result.getSimbgId());
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void fetchDocuments_ShouldReturnDocumentList() {
        // Given
        String simbgId = "SIMBG-123";
        when(simbgClient.fetchDocuments(simbgId)).thenReturn(Mono.just(documentResponses));

        // When
        List<SimbgDocumentResponse> result = simbgIntegrationService.fetchDocuments(simbgId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DOC-001", result.get(0).getDocId());
        assertEquals("PBG", result.get(0).getType());
    }

    @Test
    void updateApplication_ShouldReturnUpdateResponse() {
        // Given
        String simbgId = "SIMBG-123";
        when(simbgClient.updateApplication(eq(simbgId), any(SimbgUpdateRequest.class)))
                .thenReturn(Mono.just(updateResponse));

        // When
        SimbgUpdateResponse result = simbgIntegrationService.updateApplication(simbgId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("SIMBG-123", result.getSimbgId());
        assertEquals("UPDATED", result.getStatus());
        assertEquals("Application updated successfully", result.getMessage());
    }
}