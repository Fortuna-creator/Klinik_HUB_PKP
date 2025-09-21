package com.hub.app.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hub.app.auth.dto.LoginRequest;
import com.hub.app.auth.dto.VerifyOtpRequest;
import com.hub.app.storage.MinioService;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import com.hub.app.user.model.UserStatus;
import com.hub.app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final OtpService otpService;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final MinioService minioService;

    public User register(
            String name,
            String email,
            String phone,
            String password,
            MultipartFile ktpFile,
            MultipartFile selfieFile) {
        if (userRepo.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Upload ke MinIO
        String ktpUrl = minioService.uploadFile(ktpFile, "ktp");
        String selfieUrl = minioService.uploadFile(selfieFile, "selfie");

        User u = User.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .password(encoder.encode(password))
                .role(Role.HOMEOWNER)
                .status(UserStatus.UNVERIFIED)
                .ktpUrl(ktpUrl)
                .selfieUrl(selfieUrl)
                .build();

        userRepo.save(u);

        otpService.generateAndSendOtp(u);
        return u;
    }

    public boolean verifyOtp(VerifyOtpRequest req) {
        boolean valid = otpService.verifyOtp(req.getUserId(), req.getOtpCode());
        if (valid) {
            var user = userRepo.findById(req.getUserId()).orElseThrow();
            user.setStatus(UserStatus.PENDING_APPROVAL);
            userRepo.save(user);
        }
        return valid;
    }

    public String login(LoginRequest req) {
        var user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("User not active");
        }
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }
}
