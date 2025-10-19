package com.hub.app.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.auth.service.JwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ApiErrorResponse;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.dto.ProfileUpdateRequest;
import com.hub.app.user.dto.ProfileUpdateResponse;
import com.hub.app.user.model.Portfolio;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import com.hub.app.user.model.UserActivityStatus;
import com.hub.app.user.repository.UserRepository;
import com.hub.app.user.service.ConsultantService;
import com.hub.app.user.service.HomeOwnerService;
import com.hub.app.user.service.PortfolioService;
import com.hub.app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private PortfolioService portfolioService;

    @MockBean
    private ConsultantService consultantService;

    @MockBean
    private HomeOwnerService homeOwnerService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ApiErrorBuilder apiErrorBuilder;

    private User testUser;
    private ProfileUpdateRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPhone("1234567890");
        testUser.setRole(Role.HOMEOWNER);
        testUser.setActivityStatus(UserActivityStatus.ACTIVE);

        testRequest = new ProfileUpdateRequest();
        testRequest.setFullName("Jane Doe");
        testRequest.setEmail("jane@example.com");
        testRequest.setPhone("0987654321");
        testRequest.setAddress("456 Oak St");
        testRequest.setAvatarUrl("new-avatar.jpg");
        testRequest.setBio("Updated bio");
        testRequest.setKtpNumber("6543210987654321");
        testRequest.setKtpImageUrl("new-ktp.jpg");
    }

    @Test
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        List<User> users = List.of(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(userService).getAllUsers();
    }

    @Test
    void activateUser_ShouldActivateUserSuccessfully() throws Exception {
        when(userService.activateUser(1L)).thenReturn(testUser);

        mockMvc.perform(post("/api/user/{id}/activate", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userService).activateUser(1L);
    }

    @Test
    void deactivateUser_ShouldDeactivateUserSuccessfully() throws Exception {
        when(userService.deactivateUser(1L)).thenReturn(testUser);

        mockMvc.perform(post("/api/user/{id}/deactivate", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));

        verify(userService).deactivateUser(1L);
    }

    @Test
    void selfDeactivateUser_ShouldDeactivateUserSuccessfully() throws Exception {
        when(userService.selfDeactivateUser(1L)).thenReturn(testUser);

        mockMvc.perform(post("/api/user/{id}/self-deactivate", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));

        verify(userService).selfDeactivateUser(1L);
    }

    @Test
    void getPortfolio_ShouldReturnPortfolioWhenExists() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(portfolioService.getPortfolioByConsultant(any())).thenReturn(Optional.of(portfolio));

        mockMvc.perform(get("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));

        verify(jwtService).extractUsername("valid.token");
        verify(userRepository).findByEmail("john@example.com");
        verify(portfolioService).getPortfolioByConsultant(any());
    }

    @Test
    void getPortfolio_ShouldReturnNotFoundWhenNoPortfolio() throws Exception {
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(portfolioService.getPortfolioByConsultant(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isNotFound());

        verify(portfolioService).getPortfolioByConsultant(any());
    }

    @Test
    void getPortfolio_ShouldReturnBadRequestOnException() throws Exception {
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Database error"));
    }

    @Test
    void savePortfolio_ShouldSavePortfolioSuccessfully() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(portfolioService.saveOrUpdatePortfolio(eq(testUser), any())).thenReturn(portfolio);

        mockMvc.perform(post("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));

        verify(portfolioService).saveOrUpdatePortfolio(eq(testUser), any());
    }

    @Test
    void savePortfolio_ShouldReturnBadRequestOnDomainException() throws Exception {
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(portfolioService.saveOrUpdatePortfolio(eq(testUser), any())).thenThrow(new DomainException(ErrorCode.BAD_REQUEST, "Invalid data"));

        ResponseEntity<ApiErrorResponse> responseEntity = ResponseEntity.badRequest().build();
        when(apiErrorBuilder.error(any(HttpStatus.class), anyString(), anyString())).thenReturn(responseEntity);

        mockMvc.perform(post("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(apiErrorBuilder).error(eq(HttpStatus.BAD_REQUEST), eq("BAD_REQUEST"), eq("Invalid data"));
    }

    @Test
    void updateProfile_ShouldUpdateProfileSuccessfully() throws Exception {
        ProfileUpdateResponse response = ProfileUpdateResponse.builder()
                .userId(1L)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phone("0987654321")
                .address("456 Oak St")
                .avatarUrl("new-avatar.jpg")
                .bio("Updated bio")
                .ktpNumber("6543210987654321")
                .ktpImageUrl("new-ktp.jpg")
                .updatedAt(Instant.now())
                .build();

        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(homeOwnerService.updateProfile(1L, testRequest)).thenReturn(response);

        mockMvc.perform(post("/api/user/verification")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));

        verify(jwtService).validateToken("valid.token");
        verify(jwtService).extractUsername("valid.token");
        verify(userRepository).findByEmail("john@example.com");
        verify(homeOwnerService).updateProfile(1L, testRequest);
    }

    @Test
    void updateProfile_ShouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        when(jwtService.validateToken("invalid.token")).thenReturn(false);
        ResponseEntity<ApiErrorResponse> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        when(apiErrorBuilder.error(any(HttpStatus.class), anyString(), anyString())).thenReturn(responseEntity);

        mockMvc.perform(post("/api/user/verification")
                        .header("Authorization", "Bearer invalid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());

        verify(jwtService).validateToken("invalid.token");
        verify(apiErrorBuilder).error(eq(HttpStatus.UNAUTHORIZED), eq("UNAUTHORIZED"), eq("Unauthorized"));
        verify(homeOwnerService, never()).updateProfile(anyLong(), any());
    }

    @Test
    void updateProfile_ShouldReturnUnauthorizedWhenUserNotHomeOwner() throws Exception {
        testUser.setRole(Role.CONSULTANT);

        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        ResponseEntity<ApiErrorResponse> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        when(apiErrorBuilder.error(any(HttpStatus.class), anyString(), anyString())).thenReturn(responseEntity);

        mockMvc.perform(post("/api/user/verification")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());

        verify(apiErrorBuilder).error(eq(HttpStatus.UNAUTHORIZED), eq("UNAUTHORIZED"), eq("Access denied: Only homeowners can update their profile"));
        verify(homeOwnerService, never()).updateProfile(anyLong(), any());
    }

    @Test
    void updateProfile_ShouldReturnNotFoundWhenUserNotFound() throws Exception {
        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/user/verification")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository).findByEmail("john@example.com");
        verify(homeOwnerService, never()).updateProfile(anyLong(), any());
    }

    @Test
    void updateProfile_ShouldReturnBadRequestOnDomainException() throws Exception {
        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(homeOwnerService.updateProfile(1L, testRequest)).thenThrow(new DomainException(ErrorCode.KTP_ALREADY_USED));

        ResponseEntity<ApiErrorResponse> responseEntity = ResponseEntity.badRequest().build();
        when(apiErrorBuilder.error(any(HttpStatus.class), anyString(), anyString())).thenReturn(responseEntity);

        mockMvc.perform(post("/api/user/verification")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());

        verify(apiErrorBuilder).error(eq(HttpStatus.BAD_REQUEST), eq("KTP_ALREADY_USED"), eq("KTP number already used"));
    }

    @Test
    void updateProfile_ShouldReturnInternalServerErrorOnException() throws Exception {
        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(homeOwnerService.updateProfile(1L, testRequest)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<ApiErrorResponse> responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        when(apiErrorBuilder.error(any(HttpStatus.class), anyString(), anyString())).thenReturn(responseEntity);

        mockMvc.perform(post("/api/user/verification")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isInternalServerError());

        verify(apiErrorBuilder).error(eq(HttpStatus.INTERNAL_SERVER_ERROR), eq("INTERNAL_ERROR"), eq("Internal server error"));
    }

    @Test
    void updateProfile_ShouldValidateRequestBody() throws Exception {
        ProfileUpdateRequest invalidRequest = new ProfileUpdateRequest();
        // Missing required fields

        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/user/verification")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(homeOwnerService, never()).updateProfile(anyLong(), any());
    }
}