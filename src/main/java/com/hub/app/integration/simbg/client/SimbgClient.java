package com.hub.app.integration.simbg.client;

import com.hub.app.integration.simbg.dto.*;
import com.hub.app.integration.simbg.exception.SimbgIntegrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Client for communicating with SIMBG API.
 */
@Component
public class SimbgClient {

    private static final Logger logger = LoggerFactory.getLogger(SimbgClient.class);

    private final WebClient webClient;

    public SimbgClient(@Qualifier("simbgWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Registers a new application with SIMBG.
     *
     * @param request the registration request
     * @return the registration response
     */
    public Mono<SimbgRegistrationResponse> registerApplication(SimbgRegistrationRequest request) {
        logger.info("Registering application with SIMBG: {}", request.getExternalReference());

        return webClient.post()
                .uri("/applications")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SimbgRegistrationResponse.class)
                .doOnSuccess(response -> logger.info("Application registered successfully: {}", response.getSimbgId()))
                .doOnError(error -> handleError("Failed to register application", error));
    }

    /**
     * Tracks the status of an application.
     *
     * @param simbgId the SIMBG application ID
     * @return the status response
     */
    public Mono<SimbgStatusResponse> trackStatus(String simbgId) {
        logger.info("Tracking status for SIMBG ID: {}", simbgId);

        return webClient.get()
                .uri("/applications/{simbgId}", simbgId)
                .retrieve()
                .bodyToMono(SimbgStatusResponse.class)
                .doOnSuccess(response -> logger.info("Status retrieved for SIMBG ID: {}", simbgId))
                .doOnError(error -> handleError("Failed to track status for SIMBG ID: " + simbgId, error));
    }

    /**
     * Fetches documents for an application.
     *
     * @param simbgId the SIMBG application ID
     * @return list of document responses
     */
    public Mono<List<SimbgDocumentResponse>> fetchDocuments(String simbgId) {
        logger.info("Fetching documents for SIMBG ID: {}", simbgId);

        return webClient.get()
                .uri("/applications/{simbgId}/documents", simbgId)
                .retrieve()
                .bodyToFlux(SimbgDocumentResponse.class)
                .collectList()
                .doOnSuccess(documents -> logger.info("Fetched {} documents for SIMBG ID: {}", documents.size(), simbgId))
                .doOnError(error -> handleError("Failed to fetch documents for SIMBG ID: " + simbgId, error));
    }

    /**
     * Updates an application with new information.
     *
     * @param simbgId the SIMBG application ID
     * @param request the update request
     * @return the update response
     */
    public Mono<SimbgUpdateResponse> updateApplication(String simbgId, SimbgUpdateRequest request) {
        logger.info("Updating application for SIMBG ID: {}", simbgId);

        return webClient.post()
                .uri("/applications/{simbgId}/update", simbgId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SimbgUpdateResponse.class)
                .doOnSuccess(response -> logger.info("Application updated successfully for SIMBG ID: {}", simbgId))
                .doOnError(error -> handleError("Failed to update application for SIMBG ID: " + simbgId, error));
    }

    /**
     * Handles errors from SIMBG API calls.
     *
     * @param message the error message
     * @param error the throwable error
     */
    private void handleError(String message, Throwable error) {
        logger.error(message, error);

        if (error instanceof WebClientResponseException responseException) {
            HttpStatus status = (HttpStatus) responseException.getStatusCode();
            String responseBody = responseException.getResponseBodyAsString();

            switch (status) {
                case BAD_REQUEST:
                    throw new SimbgIntegrationException("Invalid request to SIMBG: " + responseBody, status.value());
                case UNAUTHORIZED:
                    throw new SimbgIntegrationException("Unauthorized access to SIMBG", status.value());
                case FORBIDDEN:
                    throw new SimbgIntegrationException("Forbidden access to SIMBG", status.value());
                case NOT_FOUND:
                    throw new SimbgIntegrationException("SIMBG resource not found", status.value());
                case UNPROCESSABLE_ENTITY:
                    throw new SimbgIntegrationException("Unprocessable entity in SIMBG: " + responseBody, status.value());
                default:
                    throw new SimbgIntegrationException("SIMBG API error: " + responseBody, status.value());
            }
        } else {
            throw new SimbgIntegrationException("Unexpected error communicating with SIMBG: " + error.getMessage(), error);
        }
    }
}