package com.hub.app.integration.simbg.service;

import com.hub.app.integration.simbg.dto.*;
import java.util.List;

/**
 * Service interface for SIMBG integration operations.
 */
public interface SimbgIntegrationService {

    /**
     * Registers a new application with SIMBG.
     *
     * @param request the registration request
     * @return the registration response
     */
    SimbgRegistrationResponse registerApplication(SimbgRegistrationRequest request);

    /**
     * Tracks the status of an application.
     *
     * @param simbgId the SIMBG application ID
     * @return the status response
     */
    SimbgStatusResponse trackStatus(String simbgId);

    /**
     * Fetches documents for an application.
     *
     * @param simbgId the SIMBG application ID
     * @return list of document responses
     */
    List<SimbgDocumentResponse> fetchDocuments(String simbgId);

    /**
     * Updates an application with new information.
     *
     * @param simbgId the SIMBG application ID
     * @param request the update request
     * @return the update response
     */
    SimbgUpdateResponse updateApplication(String simbgId, SimbgUpdateRequest request);
}