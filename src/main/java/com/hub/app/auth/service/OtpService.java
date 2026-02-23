package com.hub.app.auth.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hub.app.auth.model.OtpToken;
import com.hub.app.auth.repository.OtpRepository;
import com.hub.app.user.model.User;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepo;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value( "${spring.mail.host}")
    private String mailHost;
    @Value( "${spring.mail.port}")
    private Integer mailPort;
    @Value( "${spring.mail.username}")
    private String mailUsername;
    @Value( "${spring.mail.password}")
    private String mailPassword;

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

    public boolean resendOtp(String email){
        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isEmpty()){
            throw new DomainException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userOptional.get();
        return otpRepo.findTopByUserIdAndUsedFalseOrderByExpiresAtDesc(user.getId())
                .map(t -> {
                    sendEmail(user.getEmail(), t.getCode());
                    return true;
                }).orElse(false);
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
        log.info("Email config: host={}, port={}, sender={}, password={}", mailHost, mailPort, mailUsername, mailPassword);
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
