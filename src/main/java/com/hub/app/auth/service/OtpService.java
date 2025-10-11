package com.hub.app.auth.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.hub.app.auth.model.OtpToken;
import com.hub.app.auth.repository.OtpRepository;
import com.hub.app.user.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepo;
    private final JavaMailSender mailSender;

    public void generateAndSendOtp(User user) {
        String code = String.format("%06d", new SecureRandom().nextInt(999999));
        OtpToken token = OtpToken.builder()
                .userId(user.getId())
                .token(code)
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .used(false)
                .build();
        otpRepo.save(token);

        // kirim OTP via email (bisa SMS jika ada provider)
        var msg = new org.springframework.mail.SimpleMailMessage();
        msg.setTo(user.getEmail());
        msg.setSubject("OTP Registrasi HUB");
        msg.setText("Kode OTP Anda adalah: " + code);
        mailSender.send(msg);
    }

    public boolean verifyOtp(Long userId, String code) {
        return otpRepo.findTopByUserIdAndCodeAndUsedFalseOrderByExpiresAtDesc(userId, code)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .map(t -> {
                    t.setUsed(true);
                    otpRepo.save(t);
                    return true;
                }).orElse(false);
    }
}
