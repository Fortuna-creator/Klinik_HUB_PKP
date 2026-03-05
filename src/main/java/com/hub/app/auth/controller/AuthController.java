package com.hub.app.auth.controller;

import com.hub.app.auth.dto.*;
import com.hub.app.auth.exception.OtpVerificationLockedException;
import com.hub.app.auth.service.ActivationService;
import com.hub.app.auth.service.AuthService;
import com.hub.app.auth.service.OtpService;
import com.hub.app.auth.util.CookieUtil;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.DomainException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final ActivationService activationService;
    private final ApiErrorBuilder apiErrorBuilder;
    private final OtpService otpService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody InitialRegisterRequest req) {
        log.info("Register attempt for email: {}", req.getEmail());
        try {
            var user = authService.register(req);
            log.info("User registered successfully: {}", user.getEmail());
            return ResponseEntity.ok(RegisterResponse.builder()
                    .role(user.getRole())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .status(user.getStatus().name())
                    .userId(user.getId())
                    .fullName(user.getName())
                    .username(req.getUsername())
                    .build());
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument during registration for email: {}", req.getEmail(), e);
            return apiErrorBuilder.error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", e.getMessage());
        } catch (DomainException e) {
            log.error("Domain exception during registration for email: {}", req.getEmail(), e);
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            log.error("Unexpected error during registration for email: {}", req.getEmail(), e);
            return apiErrorBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred during registration");
        }
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req, HttpServletResponse response) {
        log.info("OTP verification attempt for email: {}", req.getEmail());
        try {
            var loginResponse = authService.verifyOtp(req);
            log.info("OTP verified successfully for email: {}", req.getEmail());
            
            // Set HttpOnly auth cookie with JWT token
            cookieUtil.setAuthCookie(response, loginResponse.getToken());
            
            // Return user info (without exposing token in body)
            return ResponseEntity.ok(LoginResponse.builder()
                    .userId(loginResponse.getUserId())
                    .fullName(loginResponse.getFullName())
                    .role(loginResponse.getRole())
                    .email(loginResponse.getEmail())
                    .status(loginResponse.getStatus())
                    .activityStatus(loginResponse.getActivityStatus())
                    .balaiId(loginResponse.getBalaiId())
                    .expiresIn(loginResponse.getExpiresIn())
                    // Note: token NOT included in response body (in cookie instead)
                    .build());
        } catch (OtpVerificationLockedException e) {
            log.warn("OTP verification lockout for email: {}", req.getEmail());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    new java.util.HashMap<String, Object>() {{
                        put("error", "TOO_MANY_ATTEMPTS");
                        put("message", e.getMessage());
                        put("retryAfterSeconds", e.getRemainingLockoutSeconds());
                    }}
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid OTP for email: {}", req.getEmail());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DomainException e) {
            log.error("Domain exception during OTP verification for email: {}", req.getEmail(), e);
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            log.error("Unexpected error during OTP verification for email: {}", req.getEmail(), e);
            return ResponseEntity.internalServerError().body("An error occurred during OTP verification");
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        log.info("Resend OTP attempt for email: {}", email);
        try {
            otpService.resendOtp(email);
            log.info("OTP resent successfully for email: {}", email);
            return ResponseEntity.ok("Otp email resent successfully");
        } catch (DomainException e) {
            log.error("Domain exception during OTP resend for email: {}", email, e);
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            log.error("Unexpected error during OTP resend for email: {}", email, e);
            return ResponseEntity.internalServerError().body("An error occurred during OTP resend");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        log.info("Login attempt for email: {}", req.getEmail());
        try {
            var loginResponse = authService.login(req);
            log.info("Login successful for email: {}", req.getEmail());
            
            // Set HttpOnly auth cookie with JWT token
            cookieUtil.setAuthCookie(response, loginResponse.getToken());
            
            // Return user info (without exposing token in body)
            return ResponseEntity.ok(LoginResponse.builder()
                    .userId(loginResponse.getUserId())
                    .fullName(loginResponse.getFullName())
                    .role(loginResponse.getRole())
                    .email(loginResponse.getEmail())
                    .status(loginResponse.getStatus())
                    .activityStatus(loginResponse.getActivityStatus())
                    .balaiId(loginResponse.getBalaiId())
                    .expiresIn(loginResponse.getExpiresIn())
                    // Note: token NOT included in response body (in cookie instead)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid login credentials for email: {}", req.getEmail());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DomainException e) {
            log.error("Domain exception during login for email: {}", req.getEmail(), e);
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}", req.getEmail(), e);
            return ResponseEntity.internalServerError().body("An error occurred during login");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        log.info("Logout attempt");
        try {
            // Clear the HttpOnly auth cookie
            cookieUtil.clearAuthCookie(response);
            log.info("Logout successful");
            return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
                put("message", "Logged out successfully");
            }});
        } catch (Exception e) {
            log.error("Unexpected error during logout", e);
            return ResponseEntity.internalServerError().body("An error occurred during logout");
        }
    }

    @PostMapping("/activate-account")
    public ResponseEntity<?> activateAccount(@Valid @RequestBody ActivateAccountRequest req) {
        log.info("Account activation attempt for email: {}", req.getEmail());
        try {
            var user = activationService.activateAccount(req);
            log.info("Account activated successfully for email: {}", user.getEmail());
            return ResponseEntity.ok("Account activated successfully for " + user.getEmail());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid activation request for email: {}", req.getEmail());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DomainException e) {
            log.error("Domain exception during account activation for email: {}", req.getEmail(), e);
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            log.error("Unexpected error during account activation for email: {}", req.getEmail(), e);
            return ResponseEntity.internalServerError().body("An error occurred during activation");
        }
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<?> resendActivation(@RequestParam String email) {
        log.info("Resend activation attempt for email: {}", email);
        try {
            activationService.resendActivationEmail(email);
            log.info("Activation email resent successfully for email: {}", email);
            return ResponseEntity.ok("Activation email resent successfully");
        } catch (DomainException e) {
            log.error("Domain exception during activation resend for email: {}", email, e);
            return apiErrorBuilder.error(e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getErrorCode().getDefaultMessage());
        } catch (Exception e) {
            log.error("Unexpected error during activation resend for email: {}", email, e);
            return ResponseEntity.internalServerError().body("An error occurred during activation resend");
        }
    }
}
