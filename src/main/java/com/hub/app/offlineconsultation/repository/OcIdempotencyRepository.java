package com.hub.app.offlineconsultation.repository;

import com.hub.app.offlineconsultation.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OcIdempotencyRepository extends JpaRepository<IdempotencyRecord, UUID> {

    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    /** Delete all records whose TTL has expired (used by the daily cleanup scheduler). */
    void deleteByTtlBefore(Instant cutoff);
}
