package com.hub.app.auth.controller;

import com.hub.app.auth.dto.*;
import com.hub.app.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping(value = "/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        var user = authService.register(req.getFullName(), req.getEmail(), req.getPhone(), req.getPassword());
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
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        return ResponseEntity.ok(authService.verifyOtp(req));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
