package com.hub.app.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.auth.dto.InitialRegisterRequest;
import com.hub.app.auth.dto.ProfileUpdateRequest;
import com.hub.app.auth.dto.VerifyOtpRequest;
import com.hub.app.auth.service.AuthService;
import com.hub.app.auth.service.JwtService;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void register_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        InitialRegisterRequest request = new InitialRegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        when(authService.register(anyString(), anyString())).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered, OTP sent to test@example.com"));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenPasswordsDoNotMatch() throws Exception {
        // Arrange
        InitialRegisterRequest request = new InitialRegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("different");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Passwords do not match"));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenRegistrationFails() throws Exception {
        // Arrange
        InitialRegisterRequest request = new InitialRegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        when(authService.register(anyString(), anyString())).thenThrow(new IllegalArgumentException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    void updateProfile_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String email = "test@example.com";
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        ProfileUpdateRequest profileRequest = new ProfileUpdateRequest();
        MockMultipartFile profileFile = new MockMultipartFile("profileImage", "profile.jpg", "image/jpeg", "image content".getBytes());

        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.extractUsername(validToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(authService.updateProfile(anyLong(), any(ProfileUpdateRequest.class))).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(multipart("/api/auth/profile-update")
                .file(profileFile)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated, awaiting approval"));
    }

    @Test
    void updateProfile_ShouldReturnUnauthorized_WhenInvalidToken() throws Exception {
        // Arrange
        String invalidToken = "invalid.token";
        MockMultipartFile profileFile = new MockMultipartFile("profileImage", "profile.jpg", "image/jpeg", "image content".getBytes());
        when(jwtService.validateToken(invalidToken)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(multipart("/api/auth/profile-update")
                .file(profileFile)
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void updateProfile_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String email = "test@example.com";
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.extractUsername(validToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(multipart("/api/auth/profile-update")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    @Test
    void verifyOtp_ShouldReturnOk_WhenOtpValid() throws Exception {
        // Arrange
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setUserId(1L);
        request.setOtpCode("123456");
        when(authService.verifyOtp(request)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP verified successfully"));
    }

    @Test
    void verifyOtp_ShouldReturnBadRequest_WhenOtpInvalid() throws Exception {
        // Arrange
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setUserId(1L);
        request.setOtpCode("123456");
        when(authService.verifyOtp(request)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid OTP"));
    }

    @Test
    void login_ShouldReturnToken_WhenLoginSuccessful() throws Exception {
        // Arrange
        com.hub.app.auth.dto.LoginRequest request = new com.hub.app.auth.dto.LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        String expectedToken = "jwt.token.here";
        when(authService.login(request)).thenReturn(expectedToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));
    }
}