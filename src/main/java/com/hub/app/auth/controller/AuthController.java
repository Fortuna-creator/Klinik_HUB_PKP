package com.hub.app.auth.controller;

import com.hub.app.auth.dto.*;
import com.hub.app.auth.service.AuthService;
import com.hub.app.auth.service.JwtService;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody InitialRegisterRequest req) {
        try {
            if (!req.getPassword().equals(req.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("Passwords do not match");
            }
            var user = authService.register(req.getEmail(), req.getPassword());
            return ResponseEntity.ok(RegisterResponse.builder()
                        .role(user.getRole())
                        .status(user.getStatus().name())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .userId(user.getId())
                        .fullName(user.getName())
                        .ktpUrl(user.getKtpUrl())
                        .selfieUrl(user.getSelfieUrl())
                        .username(user.getUsername())
                .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred");
        }
    }

    @PostMapping(value = "/profile-update", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader, @Valid @ModelAttribute ProfileUpdateRequest req) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(401).body("Invalid token");
            }
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            var updatedUser = authService.updateProfile(user.getId(), req);
            return ResponseEntity.ok("Profile updated, awaiting approval");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        try {
            boolean valid = authService.verifyOtp(req);
            if (valid) {
                return ResponseEntity.ok("OTP verified successfully");
            } else {
                return ResponseEntity.badRequest().body("Invalid OTP");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
