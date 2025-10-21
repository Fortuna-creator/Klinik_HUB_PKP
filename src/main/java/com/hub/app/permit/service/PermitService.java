package com.hub.app.permit.service;

import com.hub.app.integration.simbg.dto.*;
import com.hub.app.integration.simbg.service.SimbgIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling permit-related operations.
 * This service demonstrates how to integrate with SIMBG for building permit applications.
 */
@Service
public class PermitService {

    private static final Logger logger = LoggerFactory.getLogger(PermitService.class);

    @Autowired
    private SimbgIntegrationService simbgIntegrationService;

    /**
     * Submits a building permit application to SIMBG.
     * This method is called after consultation is completed and home owner wants to apply for permit.
     *
     * @param projectId the internal project ID
     * @param applicantName applicant's name
     * @param applicantIdNumber applicant's ID number
     * @param propertyAddress property address
     * @param documents list of document URLs
     * @return SIMBG registration response
     */
    public SimbgRegistrationResponse submitPermitApplication(
            Long projectId,
            String applicantName,
            String applicantIdNumber,
            String propertyAddress,
            List<DocumentDto> documents) {

        logger.info("Submitting permit application for project ID: {}", projectId);

        // Create SIMBG registration request
        SimbgRegistrationRequest request = new SimbgRegistrationRequest();
        request.setApplicantName(applicantName);
        request.setApplicantIdNumber(applicantIdNumber);
        request.setPropertyAddress(propertyAddress);
        request.setPrototypeType("NON_PROTOTYPE"); // or determine based on project type
        request.setExternalReference("PROJECT-" + projectId);
        request.setDocuments(documents);

        // Call SIMBG integration service
        SimbgRegistrationResponse response = simbgIntegrationService.registerApplication(request);

        logger.info("Permit application submitted successfully. SIMBG ID: {}", response.getSimbgId());

        // Here you would typically save the SIMBG ID to your permit/permit_application table
        // savePermitApplication(projectId, response.getSimbgId(), response.getStatus());

        return response;
    }

    /**
     * Checks the status of a permit application.
     *
     * @param simbgId the SIMBG application ID
     * @return current status from SIMBG
     */
    public SimbgStatusResponse checkPermitStatus(String simbgId) {
        logger.info("Checking permit status for SIMBG ID: {}", simbgId);

        SimbgStatusResponse status = simbgIntegrationService.trackStatus(simbgId);

        logger.info("Permit status for SIMBG ID {}: {}", simbgId, status.getStatus());

        // Here you could update your local permit status based on SIMBG status
        // updatePermitStatus(simbgId, status.getStatus());

        return status;
    }

    /**
     * Retrieves official documents for a completed permit application.
     *
     * @param simbgId the SIMBG application ID
     * @return list of official documents
     */
    public List<SimbgDocumentResponse> getPermitDocuments(String simbgId) {
        logger.info("Retrieving permit documents for SIMBG ID: {}", simbgId);

        List<SimbgDocumentResponse> documents = simbgIntegrationService.fetchDocuments(simbgId);

        logger.info("Retrieved {} documents for SIMBG ID: {}", documents.size(), simbgId);

        return documents;
    }

    /**
     * Updates a permit application with additional information or revised documents.
     *
     * @param simbgId the SIMBG application ID
     * @param comments update comments
     * @param attachments list of attachment URLs
     * @return update response
     */
    public SimbgUpdateResponse updatePermitApplication(
            String simbgId,
            String comments,
            List<AttachmentDto> attachments) {

        logger.info("Updating permit application for SIMBG ID: {}", simbgId);

        SimbgUpdateRequest request = new SimbgUpdateRequest();
        request.setComments(comments);
        request.setAttachments(attachments);

        SimbgUpdateResponse response = simbgIntegrationService.updateApplication(simbgId, request);

        logger.info("Permit application updated successfully for SIMBG ID: {}", simbgId);

        return response;
    }
}