package com.hub.app.auth.repository;

import com.hub.app.auth.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpToken, Long> {

    // Cari OTP terakhir untuk user tertentu yang masih aktif (belum digunakan)
    Optional<OtpToken> findTopByUserIdAndTokenAndUsedFalseOrderByExpiresAtDesc(Long userId, String code);

    // Hapus semua OTP lama untuk user tertentu (opsional, untuk cleanup)
    void deleteByUserId(Long userId);
}
