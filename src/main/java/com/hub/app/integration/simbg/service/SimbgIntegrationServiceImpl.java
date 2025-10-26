package com.hub.app.integration.simbg.service;

import com.hub.app.integration.simbg.client.SimbgClient;
import com.hub.app.integration.simbg.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of SIMBG integration service.
 */
@Service
@Slf4j
public class SimbgIntegrationServiceImpl implements SimbgIntegrationService {


    private final SimbgClient simbgClient;

    public SimbgIntegrationServiceImpl(SimbgClient simbgClient) {
        this.simbgClient = simbgClient;
    }

    @Override
    public SimbgRegistrationResponse registerApplication(SimbgRegistrationRequest request) {
        log.info("Registering application with SIMBG for external reference: {}", request.getExternalReference());

        try {
            SimbgRegistrationResponse response = simbgClient.registerApplication(request).block();
            log.info("Application registered successfully with SIMBG ID: {}", response.getSimbgId());
            return response;
        } catch (Exception e) {
            log.error("Failed to register application with SIMBG", e);
            throw e;
        }
    }

    @Override
    public SimbgStatusResponse trackStatus(String simbgId) {
        log.info("Tracking status for SIMBG ID: {}", simbgId);

        try {
            SimbgStatusResponse response = simbgClient.trackStatus(simbgId).block();
            log.info("Status tracked successfully for SIMBG ID: {}", simbgId);
            return response;
        } catch (Exception e) {
            log.error("Failed to track status for SIMBG ID: {}", simbgId, e);
            throw e;
        }
    }

    @Override
    public List<SimbgDocumentResponse> fetchDocuments(String simbgId) {
        log.info("Fetching documents for SIMBG ID: {}", simbgId);

        try {
            List<SimbgDocumentResponse> documents = simbgClient.fetchDocuments(simbgId).block();
            log.info("Fetched {} documents for SIMBG ID: {}", documents.size(), simbgId);
            return documents;
        } catch (Exception e) {
            log.error("Failed to fetch documents for SIMBG ID: {}", simbgId, e);
            throw e;
        }
    }

    @Override
    public SimbgUpdateResponse updateApplication(String simbgId, SimbgUpdateRequest request) {
        log.info("Updating application for SIMBG ID: {}", simbgId);

        try {
            SimbgUpdateResponse response = simbgClient.updateApplication(simbgId, request).block();
            log.info("Application updated successfully for SIMBG ID: {}", simbgId);
            return response;
        } catch (Exception e) {
            log.error("Failed to update application for SIMBG ID: {}", simbgId, e);
            throw e;
        }
    }
}