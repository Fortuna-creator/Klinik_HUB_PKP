package com.hub.app.auth.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.hub.app.auth.exception.OtpVerificationLockedException;
import com.hub.app.auth.model.OtpAttempt;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hub.app.auth.model.OtpToken;
import com.hub.app.auth.repository.OtpAttemptRepository;
import com.hub.app.auth.repository.OtpRepository;
import com.hub.app.user.model.User;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepo;
    private final OtpAttemptRepository otpAttemptRepo;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MINUTES = 15;

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

        String code = String.format("%06d", new SecureRandom().nextInt(999999));
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

        // Invalidate any existing unused OTP so the old code can no longer be used
        otpRepo.findTopByUserIdAndUsedFalseOrderByExpiresAtDesc(user.getId())
                .ifPresent(t -> {
                    t.setUsed(true);
                    otpRepo.save(t);
                });

        // Generate and send a brand-new OTP (subject to the existing rate limit)
        generateAndSendOtp(user);
        return true;
    }

    public boolean verifyOtp(Long userId, String code) {
        // Check if user is locked out
        Optional<OtpAttempt> attemptOpt = otpAttemptRepo.findByUserId(userId);
        if (attemptOpt.isPresent()) {
            OtpAttempt attempt = attemptOpt.get();
            if (attempt.isLockedOut()) {
                long remainingSeconds = attempt.getRemainingLockoutSeconds();
                log.warn("OTP verification locked for userId: {} (remaining: {} seconds)", userId, remainingSeconds);
                throw new OtpVerificationLockedException(
                        "Too many failed OTP attempts. Please try again in " + remainingSeconds + " seconds.",
                        attempt.getLockoutExpiresAt()
                );
            }
        }

        boolean isValid = otpRepo.findTopByUserIdAndCodeAndUsedFalseOrderByExpiresAtDesc(userId, code)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .isPresent();

        if (isValid) {
            // Success: mark OTP as used and reset attempts
            otpRepo.findTopByUserIdAndCodeAndUsedFalseOrderByExpiresAtDesc(userId, code)
                    .ifPresent(t -> {
                        t.setUsed(true);
                        otpRepo.save(t);
                    });

            // Reset failed attempts on successful verification
            attemptOpt.ifPresent(attempt -> {
                attempt.reset();
                otpAttemptRepo.save(attempt);
            });

            log.info("OTP verified successfully for userId: {}", userId);
            return true;
        } else {
            // Failed attempt: track and potentially lock out
            OtpAttempt attempt = attemptOpt.orElseGet(() -> 
                OtpAttempt.builder()
                        .userId(userId)
                        .failedAttempts(0)
                        .build()
            );

            attempt.recordFailedAttempt();
            log.warn("OTP verification failed for userId: {} (attempt: {})", userId, attempt.getFailedAttempts());

            if (attempt.getFailedAttempts() >= MAX_OTP_ATTEMPTS) {
                attempt.lockOut();
                otpAttemptRepo.save(attempt);
                log.warn("User {} locked out after {} failed OTP attempts", userId, MAX_OTP_ATTEMPTS);
                throw new OtpVerificationLockedException(
                        "Too many failed OTP attempts. Account locked for 15 minutes.",
                        attempt.getLockoutExpiresAt()
                );
            }

            otpAttemptRepo.save(attempt);
            throw new DomainException(ErrorCode.AUTH_INVALID_OTP);
        }
    }

    @Async
    public void sendEmail(String email, String code){
        log.info("Email config: host={}, port={}, sender={}", mailHost, mailPort, mailUsername);
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
