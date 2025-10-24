package com.hub.app.auth.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
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
        // Rate limiting: max 3 OTP per hour per user
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long recentOtps = otpRepo.findAll().stream()
                .filter(t -> t.getUserId().equals(user.getId()) && t.getExpiresAt().isAfter(oneHourAgo))
                .count();
        if (recentOtps >= 3) {
            throw new IllegalArgumentException("Too many OTP requests. Please try again later.");
        }

        String code = String.format("%04d", new SecureRandom().nextInt(9999));
        OtpToken token = OtpToken.builder()
                .userId(user.getId())
                .code(code)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .used(false)
                .build();
        otpRepo.save(token);

        // kirim OTP via email (bisa SMS jika ada provider)
        sendEmail(user.getEmail(), code);
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

    @Async
    public void sendEmail(String email, String code){
        try{
            var msg = new org.springframework.mail.SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("OTP Registrasi HUB");
            msg.setText("Kode OTP Anda adalah: " + code);
            mailSender.send(msg);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
