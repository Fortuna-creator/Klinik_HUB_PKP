package com.hub.app.auth.repository;

import com.hub.app.auth.model.OtpAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpAttemptRepository extends JpaRepository<OtpAttempt, Long> {
    Optional<OtpAttempt> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
