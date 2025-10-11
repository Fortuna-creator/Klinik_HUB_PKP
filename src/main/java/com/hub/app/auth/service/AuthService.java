package com.hub.app.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.hub.app.auth.dto.LoginRequest;
import com.hub.app.auth.dto.VerifyOtpRequest;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import com.hub.app.user.model.UserStatus;
import com.hub.app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
//    private final MinioService minioService;
 
    @Transactional
    public User register(
            String name,
            String email,
            String phone,
            String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // TODO: pindahin ini ke tempat lain
//        String ktpUrl = minioService.uploadFile(ktpFile, "ktp");
//        String selfieUrl = minioService.uploadFile(selfieFile, "selfie");

        User u = User.builder()
                .name(name)
                .email(email)
                .username(email)
                .phone(phone)
                .password(encoder.encode(password))
                // TODO: registration using ROLE from request to accommodate web registration or bulk registration from web
                .role(Role.HOMEOWNER) 
                .status(UserStatus.UNVERIFIED)
//                .ktpUrl(ktpUrl)
//                .selfieUrl(selfieUrl)
                .build();

        userRepository.save(u);

        otpService.generateAndSendOtp(u);
        return u;
    }
    @Transactional

    public boolean verifyOtp(VerifyOtpRequest req) {
        boolean valid = otpService.verifyOtp(req.getUserId(), req.getOtpCode());
        if (valid) {
            User user = userRepository.findById(req.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setStatus(UserStatus.PENDING_APPROVAL);
            userRepository.save(user);
        }
        return valid;
    }

    public String login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User not active");
        }
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }
}