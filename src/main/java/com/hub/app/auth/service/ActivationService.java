package com.hub.app.auth.service;

import com.hub.app.auth.dto.ActivateAccountRequest;
import com.hub.app.auth.model.ActivationToken;
import com.hub.app.auth.repository.ActivationTokenRepository;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.model.User;
import com.hub.app.user.model.UserActivityStatus;
import com.hub.app.user.model.VerificationStatus;
import com.hub.app.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivationService {

    private final UserRepository userRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User activateAccount(ActivateAccountRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "Passwords do not match");
        }

        // Validate password strength
        if (request.getPassword().length() < 8) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "Password must be at least 8 characters");
        }

        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            throw new DomainException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userOptional.get();

        // Check if user is already activated
        if (user.getStatus() == VerificationStatus.VERIFIED) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "Account is already activated");
        }

        // Find and validate activation token
        Optional<ActivationToken> tokenOptional = activationTokenRepository
                .findByUserIdAndTokenAndUsedFalse(user.getId(), request.getToken());

        if (tokenOptional.isEmpty()) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "Invalid or expired activation token");
        }

        ActivationToken token = tokenOptional.get();

        // Check if token is expired
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "Activation token has expired");
        }

        // Activate account
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(VerificationStatus.VERIFIED);
        user.setActivityStatus(UserActivityStatus.ACTIVE);

        // Mark token as used
        token.setUsed(true);

        userRepository.save(user);
        activationTokenRepository.save(token);

        log.info("Account activated for user: {}", user.getEmail());

        return user;
    }

    /**
     * Resend activation email if token is expired or lost
     */
    @Transactional
    public void resendActivationEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new DomainException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userOptional.get();

        // Check if user is already activated
        if (user.getStatus() == VerificationStatus.VERIFIED) {
            throw new DomainException(ErrorCode.BAD_REQUEST, "Account is already activated");
        }

        // Generate new activation token
        String activationToken = java.util.UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(24, java.time.temporal.ChronoUnit.HOURS);

        ActivationToken token = ActivationToken.builder()
                .userId(user.getId())
                .token(activationToken)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        activationTokenRepository.save(token);

        // Send email (implementation would go here - similar to BalaiService.sendActivationEmail)
        log.info("Resent activation email to: {}", user.getEmail());
    }
}