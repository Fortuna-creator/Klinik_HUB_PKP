package com.hub.app.offlineconsultation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.offlineconsultation.dto.response.IdempotencyConflictResponse;
import com.hub.app.offlineconsultation.entity.IdempotencyRecord;
import com.hub.app.offlineconsultation.entity.IdempotencyStatus;
import com.hub.app.offlineconsultation.repository.OcIdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Manages the idempotency lifecycle for state-changing operations.
 *
 * <h3>Flow (per the API contract):</h3>
 * <ol>
 *   <li><b>Before processing</b> — call {@link #checkAndCreate}.
 *       If an existing record is found with status PROCESSING → return 409.
 *       If an existing record is found with status SUCCESS → return cached response.
 *       If an existing record is found with status FAILED → allow retry (delete old, create new).
 *       If no record exists → create a new PROCESSING record and return empty (caller proceeds).
 *   </li>
 *   <li><b>After processing</b> — call {@link #markSuccess} with the serialised response body + HTTP status.</li>
 *   <li><b>On failure</b> — call {@link #markFailed} so the request can be retried with the same key.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final OcIdempotencyRepository repository;
    private final ObjectMapper objectMapper;

    // ── Public API ───────────────────────────────────────────────────────

    /**
     * Checks for an existing idempotency record.
     *
     * @return {@link Optional#empty()} if this is a new request (a PROCESSING record has been created).
     *         Otherwise returns a {@code ResponseEntity} that should be returned immediately to the client.
     */
    @Transactional
    public Optional<ResponseEntity<Object>> checkAndCreate(
            String idempotencyKey,
            Long userId,
            String endpoint) {

        Optional<IdempotencyRecord> existing = repository.findByIdempotencyKey(idempotencyKey);

        if (existing.isEmpty()) {
            // New request → create PROCESSING record
            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .userId(userId)
                    .endpoint(endpoint)
                    .status(IdempotencyStatus.PROCESSING)
                    .build();
            repository.save(record);
            log.debug("Idempotency: new record created for key={}", idempotencyKey);
            return Optional.empty();
        }

        IdempotencyRecord record = existing.get();

        return switch (record.getStatus()) {
            case PROCESSING -> {
                log.warn("Idempotency: key={} is still PROCESSING", idempotencyKey);
                IdempotencyConflictResponse body = IdempotencyConflictResponse.builder()
                        .error("REQUEST_DUPLICATE")
                        .message("Same request already being processed")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .idempotencyKey(idempotencyKey)
                        .status("PROCESSING")
                        .build();
                yield Optional.of(ResponseEntity.status(HttpStatus.CONFLICT).body(castToObject(body)));
            }
            case SUCCESS -> {
                log.debug("Idempotency: key={} already SUCCESS, returning cached response", idempotencyKey);
                Object cachedBody = deserializeCachedResponse(record.getResponseBody());
                HttpStatus cachedStatus = HttpStatus.valueOf(
                        record.getHttpStatus() != null ? record.getHttpStatus() : 200);
                yield Optional.of(ResponseEntity.status(cachedStatus).body(cachedBody));
            }
            case FAILED -> {
                log.info("Idempotency: key={} previously FAILED, allowing retry", idempotencyKey);
                repository.delete(record);
                repository.flush();
                IdempotencyRecord retryRecord = IdempotencyRecord.builder()
                        .idempotencyKey(idempotencyKey)
                        .userId(userId)
                        .endpoint(endpoint)
                        .status(IdempotencyStatus.PROCESSING)
                        .build();
                repository.save(retryRecord);
                yield Optional.empty();
            }
        };
    }

    /**
     * Marks the idempotency record as SUCCESS and caches the response.
     */
    @Transactional
    public void markSuccess(String idempotencyKey, Object responseBody, int httpStatus) {
        repository.findByIdempotencyKey(idempotencyKey).ifPresent(record -> {
            record.setStatus(IdempotencyStatus.SUCCESS);
            record.setResponseBody(serialize(responseBody));
            record.setHttpStatus(httpStatus);
            repository.save(record);
            log.debug("Idempotency: key={} marked SUCCESS", idempotencyKey);
        });
    }

    /**
     * Marks the idempotency record as FAILED so it can be retried.
     */
    @Transactional
    public void markFailed(String idempotencyKey) {
        repository.findByIdempotencyKey(idempotencyKey).ifPresent(record -> {
            record.setStatus(IdempotencyStatus.FAILED);
            repository.save(record);
            log.debug("Idempotency: key={} marked FAILED", idempotencyKey);
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize idempotency response", e);
            return "{}";
        }
    }

    private Object deserializeCachedResponse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            // Return as a generic map so Jackson re-serialises it cleanly
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached idempotency response", e);
            return json;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object castToObject(Object o) {
        return o;
    }
}
