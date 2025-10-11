package com.hub.app.auth.controller;

import com.hub.app.auth.dto.InitialRegisterRequest;
import com.hub.app.auth.dto.LoginRequest;
import com.hub.app.auth.dto.ProfileUpdateRequest;
import com.hub.app.auth.dto.VerifyOtpRequest;
import com.hub.app.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody InitialRegisterRequest req) {
        try {
            if (!req.getPassword().equals(req.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("Passwords do not match");
            }
            var user = authService.register(req.getEmail(), req.getPassword());
            return ResponseEntity.ok("User registered, OTP sent to " + user.getEmail());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred");
        }
    }

    @PostMapping(value = "/profile-update", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProfile(@RequestPart("userId") Long userId, @Valid @ModelAttribute ProfileUpdateRequest req) {
        try {
            var user = authService.updateProfile(userId, req);
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
    public ResponseEntity<String> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
