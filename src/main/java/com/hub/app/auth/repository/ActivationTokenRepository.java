package com.hub.app.auth.repository;

import com.hub.app.auth.model.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {

    // Find valid activation token for user
    Optional<ActivationToken> findByUserIdAndTokenAndUsedFalse(Long userId, String token);

    // Clean up expired tokens
    void deleteByExpiresAtBefore(java.time.Instant instant);
}