package com.hub.app.offlineconsultation.config;

import com.hub.app.offlineconsultation.repository.OcIdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Daily cleanup job that purges expired idempotency records
 * (those whose {@code ttl < now()}).
 *
 * Runs once every hour to keep the table lean, per the contract's
 * recommendation of a 24-hour TTL.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCleanupScheduler {

    private final OcIdempotencyRepository repository;

    /**
     * Runs every hour. Deletes all idempotency records whose TTL has passed.
     */
    @Scheduled(fixedRate = 3_600_000) // every 60 minutes
    @Transactional
    public void purgeExpiredRecords() {
        Instant now = Instant.now();
        repository.deleteByTtlBefore(now);
        log.info("Idempotency cleanup: purged expired records (before {})", now);
    }
}
