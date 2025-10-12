package com.hub.app.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<<<<<<< HEAD

=======
>>>>>>> develop

import com.hub.app.auth.dto.LoginRequest;
import com.hub.app.auth.dto.ProfileUpdateRequest;
import com.hub.app.auth.dto.VerifyOtpRequest;
import com.hub.app.storage.MinioService;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import com.hub.app.user.model.UserActivityStatus;
import com.hub.app.user.model.VerificationStatus;
import com.hub.app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final MinioService minioService;
 
    @Transactional
    public User register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User u = User.builder()
                .email(email)
                .password(encoder.encode(password))
                .role(Role.HOMEOWNER)
                .status(VerificationStatus.UNVERIFIED)
                .activityStatus(UserActivityStatus.ACTIVE)
                .build();

        userRepository.save(u);

        otpService.generateAndSendOtp(u);
        return u;
    }

    @Transactional
    public User updateProfile(Long userId, ProfileUpdateRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getStatus() != VerificationStatus.UNVERIFIED) {
            throw new IllegalArgumentException("User not eligible for profile update");
        }

        // Upload files if provided
        String ktpUrl = req.getKtpFile() != null ? minioService.uploadFile(req.getKtpFile(), "ktp") : null;
        String selfieUrl = req.getSelfieFile() != null ? minioService.uploadFile(req.getSelfieFile(), "selfie") : null;

        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setAddress(req.getAddress());
        user.setKtpNumber(req.getKtpNumber());
        if (ktpUrl != null) user.setKtpUrl(ktpUrl);
        if (selfieUrl != null) user.setSelfieUrl(selfieUrl);
        user.setStatus(VerificationStatus.VERIFICATION_IN_PROCESS);

        return userRepository.save(user);
    }
    @Transactional

    public boolean verifyOtp(VerifyOtpRequest req) {
        boolean valid = otpService.verifyOtp(req.getUserId(), req.getOtpCode());
        if (valid) {
            User user = userRepository.findById(req.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
            // Status remains UNVERIFIED after OTP verification
        }
        return valid;
    }

    public String login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (user.getStatus() != VerificationStatus.VERIFIED) {
            throw new IllegalArgumentException("User not verified");
        }
        if (user.getActivityStatus() != UserActivityStatus.ACTIVE) {
            throw new IllegalArgumentException("User account is inactive");
        }
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }
}