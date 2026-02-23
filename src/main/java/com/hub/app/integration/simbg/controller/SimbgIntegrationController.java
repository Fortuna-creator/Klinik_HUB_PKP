package com.hub.app.integration.simbg.controller;

import com.hub.app.integration.simbg.dto.*;
import com.hub.app.integration.simbg.service.SimbgIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for SIMBG integration operations.
 */
@RestController
@RequestMapping("/api/integration/simbg")
@Tag(name = "SIMBG Integration", description = "API for integrating with SIMBG (Sistem Informasi Manajemen Bangunan Gedung)")
@Slf4j
public class SimbgIntegrationController {


    private final SimbgIntegrationService simbgIntegrationService;

    public SimbgIntegrationController(SimbgIntegrationService simbgIntegrationService) {
        this.simbgIntegrationService = simbgIntegrationService;
    }

    /**
     * Registers a new application with SIMBG.
     *
     * @param request the registration request
     * @return the registration response
     */
    @PostMapping("/register")
    @Operation(summary = "Register new application", description = "Register a new building permit application with SIMBG")
    public ResponseEntity<SimbgRegistrationResponse> registerApplication(@RequestBody SimbgRegistrationRequest request) {
        log.info("Received request to register application with SIMBG");
        SimbgRegistrationResponse response = simbgIntegrationService.registerApplication(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Tracks the status of an application.
     *
     * @param simbgId the SIMBG application ID
     * @return the status response
     */
    @GetMapping("/status/{simbgId}")
    @Operation(summary = "Track application status", description = "Get the current status of a SIMBG application")
    public ResponseEntity<SimbgStatusResponse> trackStatus(@PathVariable String simbgId) {
        log.info("Received request to track status for SIMBG ID: {}", simbgId);
        SimbgStatusResponse response = simbgIntegrationService.trackStatus(simbgId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetches documents for an application.
     *
     * @param simbgId the SIMBG application ID
     * @return list of document responses
     */
    @GetMapping("/documents/{simbgId}")
    @Operation(summary = "Fetch application documents", description = "Get all documents associated with a SIMBG application")
    public ResponseEntity<List<SimbgDocumentResponse>> fetchDocuments(@PathVariable String simbgId) {
        log.info("Received request to fetch documents for SIMBG ID: {}", simbgId);
        List<SimbgDocumentResponse> documents = simbgIntegrationService.fetchDocuments(simbgId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Updates an application with new information.
     *
     * @param simbgId the SIMBG application ID
     * @param request the update request
     * @return the update response
     */
    @PostMapping("/update/{simbgId}")
    @Operation(summary = "Update application", description = "Update a SIMBG application with new information or documents")
    public ResponseEntity<SimbgUpdateResponse> updateApplication(@PathVariable String simbgId, @RequestBody SimbgUpdateRequest request) {
        log.info("Received request to update application for SIMBG ID: {}", simbgId);
        SimbgUpdateResponse response = simbgIntegrationService.updateApplication(simbgId, request);
        return ResponseEntity.ok(response);
    }
}