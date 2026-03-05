package com.hub.app.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.auth.dto.InitialRegisterRequest;
import com.hub.app.auth.dto.LoginResponse;
import com.hub.app.auth.dto.VerifyOtpRequest;
import com.hub.app.auth.service.ActivationService;
import com.hub.app.auth.service.AuthService;
import com.hub.app.auth.service.JwtService;
import com.hub.app.auth.service.OtpService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.model.HomeOwner;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ActivationService activationService;

    @MockitoBean
    private ApiErrorBuilder apiErrorBuilder;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void register_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        InitialRegisterRequest request = new InitialRegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setPhone("081234567890");
        request.setAddress("Test Address");
        request.setNik("1234567890123456");
        request.setRole("Warga");
        
        HomeOwner mockHomeOwner = new HomeOwner();
        mockHomeOwner.setId(1L);
        mockHomeOwner.setEmail("test@example.com");
        mockHomeOwner.setName("Test User");
        mockHomeOwner.setPhone("081234567890");
        
        when(authService.register(any(InitialRegisterRequest.class))).thenReturn(mockHomeOwner);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenEmailAlreadyExists() throws Exception {
        // Arrange
        InitialRegisterRequest request = new InitialRegisterRequest();
        request.setUsername("testuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setPhone("081234567890");
        request.setAddress("Test Address");
        request.setNik("1234567890123456");
        request.setRole("Warga");

        when(authService.register(any(InitialRegisterRequest.class)))
                .thenThrow(new DomainException(ErrorCode.USER_EMAIL_ALREADY_EXISTS));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Arrange - Missing required fields
        String invalidRequest = "{ \"email\": \"test@example.com\" }";

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }


    @Test
    void verifyOtp_ShouldReturnOk_WhenOtpValid() throws Exception {
        // Arrange
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@test.com");
        request.setOtpCode("1234");

        String expectedToken = "jwt.token.here";
        LoginResponse expectation = LoginResponse.builder()
                .userId(1L)
                .fullName("example")
                .token(expectedToken)
                .role("HOMEOWNER")
                .status("ACTIVE")
                .email("test@test.com")
                .build();

        when(authService.verifyOtp(request)).thenReturn(expectation);

        // Act & Assert
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP verified successfully"));
    }

    @Test
    void verifyOtp_ShouldReturnUnauthorized_AndApiErrorResponse_WhenOtpInvalid() throws Exception {
        // Arrange
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@test.com");
        request.setOtpCode("1235");

        // Service throws a domain-level error for invalid OTP
        doThrow(new DomainException(ErrorCode.AUTH_INVALID_OTP))
                .when(authService).verifyOtp(request);

        // Act & Assert
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_OTP"))
                .andExpect(jsonPath("$.message").value("Invalid OTP"));
    }

    @Test
    void login_ShouldReturnToken_WhenLoginSuccessful() throws Exception {
        // Arrange
        com.hub.app.auth.dto.LoginRequest request = new com.hub.app.auth.dto.LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        String expectedToken = "jwt.token.here";

        LoginResponse expectation = LoginResponse.builder()
                .userId(1L)
                .fullName("example")
                .token(expectedToken)
                .role("HOMEOWNER")
                .status("ACTIVE")
                .email("test@example.com")
                .build();

        when(authService.login(request)).thenReturn(expectation);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectation)));
    }
}