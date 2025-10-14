package com.hub.app.auth.controller;

import com.hub.app.auth.dto.*;
import com.hub.app.auth.service.ActivationService;
import com.hub.app.auth.service.AuthService;
import com.hub.app.auth.service.JwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.DomainException;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final ActivationService activationService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ApiErrorBuilder apiErrorBuilder;

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
                .build());
        } catch (IllegalArgumentException e) {
            return apiErrorBuilder.error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", e.getMessage());
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return apiErrorBuilder.error(HttpStatus.BAD_REQUEST, e.getMessage(), e.getMessage());
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
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        try {
            return ResponseEntity.ok(authService.verifyOtp(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/activate-account")
    public ResponseEntity<?> activateAccount(@Valid @RequestBody ActivateAccountRequest req) {
        try {
            var user = activationService.activateAccount(req);
            return ResponseEntity.ok("Account activated successfully for " + user.getEmail());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred during activation");
        }
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<?> resendActivation(@RequestParam String email) {
        try {
            activationService.resendActivationEmail(email);
            return ResponseEntity.ok("Activation email resent successfully");
        } catch (DomainException e) {
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred");
        }
    }
}
