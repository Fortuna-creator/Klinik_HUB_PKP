package com.hub.app.auth.service;

import com.hub.app.auth.dto.InitialRegisterRequest;
import com.hub.app.auth.dto.LoginResponse;
import com.hub.app.auth.repository.OtpRepository;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.model.HomeOwner;
import com.hub.app.user.model.VerificationStatus;
import jakarta.persistence.EntityManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hub.app.auth.dto.LoginRequest;
import com.hub.app.auth.dto.VerifyOtpRequest;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import com.hub.app.user.model.UserActivityStatus;
import com.hub.app.user.repository.HomeOwnerRepository;
import com.hub.app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final HomeOwnerRepository homeOwnerRepository;
    private final OtpService otpService;
    private final OtpRepository otpRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final EntityManager entityManager;
 
    @Transactional
    public HomeOwner register(InitialRegisterRequest req) {
        // Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(req.getEmail());
        
        // If email exists and user is UNVERIFIED, allow re-registration
        // by deleting the old unverified account (fresh start with new OTP)
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getStatus() == VerificationStatus.UNVERIFIED) {
                // Delete OTP tokens first (foreign key dependency)
                otpRepository.deleteByUserId(user.getId());
                // Then delete the old unverified account to allow fresh registration
                userRepository.delete(user);
                // Flush to ensure the delete is committed before creating new user
                entityManager.flush();
            } else {
                // If verified or verified by other means, block the registration
                throw new DomainException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
            }
        }

        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setName(req.getFullName());
        homeOwner.setEmail(req.getEmail());
        homeOwner.setPassword(encoder.encode(req.getPassword()));
        homeOwner.setPhone(req.getPhone());
        homeOwner.setAddress(req.getAddress());
        homeOwner.setKtpNumber(req.getNik());
        
        // Determine role from the request
        if ("Balai".equalsIgnoreCase(req.getRole())) {
            homeOwner.setRole(Role.BALAI);
        } else {
            homeOwner.setRole(Role.HOMEOWNER);
        }
        
        homeOwner.setStatus(VerificationStatus.UNVERIFIED);
        homeOwner.setActivityStatus(UserActivityStatus.INACTIVE);

        homeOwnerRepository.save(homeOwner);

        otpService.generateAndSendOtp(homeOwner);
        return homeOwner;
    }


    @Transactional
    public LoginResponse verifyOtp(VerifyOtpRequest req) {
        Optional<User> userOptional = userRepository.findByEmail(req.getEmail());

        if (userOptional.isEmpty()) {
            throw new DomainException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userOptional.get();

        boolean valid = otpService.verifyOtp(user.getId(), req.getOtpCode());


        if (valid) {
            user.setActivityStatus(UserActivityStatus.ACTIVE);
            user.setStatus(VerificationStatus.VERIFIED);
            userRepository.save(user);
        } else {
            throw new DomainException(ErrorCode.AUTH_INVALID_OTP);
        }

        return setLoginResponse(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new DomainException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        if (user.getActivityStatus() != UserActivityStatus.ACTIVE) {
            throw new DomainException(ErrorCode.AUTH_INVALID_STATUS);
        }

        return setLoginResponse(user);
    }

    private LoginResponse setLoginResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        Long expires = jwtService.getJwtExpirationMs();

        return LoginResponse.builder()
                .userId(user.getId())
                .fullName(user.getName())
                .token(token)
                .role(user.getRole().name())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .activityStatus(user.getActivityStatus().name())
                .balaiId(user.getBalaiId())
                .expiresIn(System.currentTimeMillis() + expires)
                .build();
    }
}