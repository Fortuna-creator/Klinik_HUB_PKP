package com.hub.app.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.auth.service.JwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ApiErrorResponse;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.user.dto.ConsultantListResponse;
import com.hub.app.user.dto.ConsultantPortfolioResponse;
import com.hub.app.user.dto.PortfolioProjectRequest;
import com.hub.app.user.dto.ProfileUpdateRequest;
import com.hub.app.user.dto.ProfileUpdateResponse;
import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.Portfolio;
import com.hub.app.user.model.Role;
import com.hub.app.user.model.User;
import com.hub.app.user.model.UserActivityStatus;
import com.hub.app.user.repository.UserRepository;
import com.hub.app.storage.MinioService;
import com.hub.app.user.service.ConsultantService;
import com.hub.app.user.service.HomeOwnerService;
import com.hub.app.user.service.PortfolioService;
import com.hub.app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PortfolioService portfolioService;

    @MockitoBean
    private ConsultantService consultantService;

    @MockitoBean
    private HomeOwnerService homeOwnerService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ApiErrorBuilder apiErrorBuilder;

    @MockitoBean
    private MinioService minioService;

    private User testUser;
    private Consultant testConsultant;
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

        testConsultant = new Consultant();
        testConsultant.setId(2L);
        testConsultant.setName("Consultant Jane");
        testConsultant.setEmail("consultant@example.com");
        testConsultant.setPhone("0987654321");
        testConsultant.setRole(Role.CONSULTANT);
        testConsultant.setActivityStatus(UserActivityStatus.ACTIVE);

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
    void getPortfolios_ShouldReturnPortfoliosWhenExists() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(portfolioService.getPortfoliosByConsultant(any())).thenReturn(List.of(portfolio));

        mockMvc.perform(get("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(jwtService).extractUsername("valid.token");
        verify(userRepository).findByEmail("john@example.com");
        verify(portfolioService).getPortfoliosByConsultant(any());
    }

    @Test
    void getPortfolios_ShouldReturnNotFoundWhenNoPortfolios() throws Exception {
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(portfolioService.getPortfoliosByConsultant(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isNotFound());

        verify(portfolioService).getPortfoliosByConsultant(any());
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

    @Test
    void getPortfolio_ShouldReturnPortfolioWhenExists() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        when(jwtService.extractUsername("valid.token")).thenReturn("consultant@example.com");
        when(userRepository.findByEmail("consultant@example.com")).thenReturn(Optional.of(testConsultant));
        when(portfolioService.getPortfolioByConsultant(any())).thenReturn(Optional.of(portfolio));

        mockMvc.perform(get("/api/user/portfolio/first")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));

        verify(jwtService).extractUsername("valid.token");
        verify(userRepository).findByEmail("consultant@example.com");
        verify(portfolioService).getPortfolioByConsultant(any());
    }

    @Test
    void getPortfolio_ShouldReturnNotFoundWhenNoPortfolio() throws Exception {
        when(jwtService.extractUsername("valid.token")).thenReturn("consultant@example.com");
        when(userRepository.findByEmail("consultant@example.com")).thenReturn(Optional.of(testConsultant));
        when(portfolioService.getPortfolioByConsultant(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/portfolio/first")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isNotFound());

        verify(portfolioService).getPortfolioByConsultant(any());
    }

    @Test
    void getPortfolio_ShouldReturnBadRequestWhenUserNotConsultant() throws Exception {
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/user/portfolio/first")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isBadRequest());

        verify(userRepository).findByEmail("john@example.com");
        verify(portfolioService, never()).getPortfolioByConsultant(any());
    }

    @Test
    void getPortfolio_ShouldReturnInternalServerErrorOnException() throws Exception {
        when(jwtService.extractUsername("valid.token")).thenReturn("consultant@example.com");
        when(userRepository.findByEmail("consultant@example.com")).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/user/portfolio/first")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getConsultantPortfolio_ShouldReturnPortfolioWhenExists() throws Exception {
        ConsultantPortfolioResponse response = ConsultantPortfolioResponse.builder()
                .consultant(ConsultantPortfolioResponse.ConsultantInfo.builder()
                        .id(2L)
                        .name("Consultant Jane")
                        .email("consultant@example.com")
                        .build())
                .portfolios(List.of())
                .build();

        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(portfolioService.getConsultantPortfolioForHomeowner(2L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/user/consultants/{consultantId}/portfolio", 2L)
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.consultant.id").value(2L))
                .andExpect(jsonPath("$.consultant.name").value("Consultant Jane"));

        verify(jwtService).validateToken("valid.token");
        verify(portfolioService).getConsultantPortfolioForHomeowner(2L);
    }

    @Test
    void getConsultantPortfolio_ShouldReturnNotFoundWhenNoPortfolio() throws Exception {
        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(portfolioService.getConsultantPortfolioForHomeowner(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/consultants/{consultantId}/portfolio", 2L)
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isNotFound());

        verify(portfolioService).getConsultantPortfolioForHomeowner(2L);
    }

    @Test
    void getConsultantPortfolio_ShouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        when(jwtService.validateToken("invalid.token")).thenReturn(false);
        ResponseEntity<ApiErrorResponse> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        when(apiErrorBuilder.error(any(HttpStatus.class), anyString(), anyString())).thenReturn(responseEntity);

        mockMvc.perform(get("/api/user/consultants/{consultantId}/portfolio", 2L)
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());

        verify(jwtService).validateToken("invalid.token");
        verify(apiErrorBuilder).error(eq(HttpStatus.UNAUTHORIZED), eq("UNAUTHORIZED"), eq("Unauthorized"));
        verify(portfolioService, never()).getConsultantPortfolioForHomeowner(anyLong());
    }

    @Test
    void getConsultantPortfolio_ShouldReturnInternalServerErrorOnException() throws Exception {
        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(portfolioService.getConsultantPortfolioForHomeowner(2L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/user/consultants/{consultantId}/portfolio", 2L)
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updatePortfolio_ShouldUpdatePortfolioSuccessfully() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        when(jwtService.extractUsername("valid.token")).thenReturn("consultant@example.com");
        when(userRepository.findByEmail("consultant@example.com")).thenReturn(Optional.of(testConsultant));
        when(portfolioService.saveOrUpdatePortfolio(eq(testConsultant), any())).thenReturn(portfolio);

        mockMvc.perform(put("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));

        verify(portfolioService).saveOrUpdatePortfolio(eq(testConsultant), any());
    }

    @Test
    void updatePortfolio_ShouldReturnBadRequestWhenUserNotConsultant() throws Exception {
        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(portfolioService, never()).saveOrUpdatePortfolio(any(), any());
    }

    @Test
    void updatePortfolio_ShouldReturnBadRequestOnDomainException() throws Exception {
        when(jwtService.extractUsername("valid.token")).thenReturn("consultant@example.com");
        when(userRepository.findByEmail("consultant@example.com")).thenReturn(Optional.of(testConsultant));
        when(portfolioService.saveOrUpdatePortfolio(eq(testConsultant), any())).thenThrow(new DomainException(ErrorCode.BAD_REQUEST, "Invalid data"));

        ResponseEntity<ApiErrorResponse> responseEntity = ResponseEntity.badRequest().build();
        when(apiErrorBuilder.error(any(HttpStatus.class), anyString(), anyString())).thenReturn(responseEntity);

        mockMvc.perform(put("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(apiErrorBuilder).error(eq(HttpStatus.BAD_REQUEST), eq("BAD_REQUEST"), eq("Invalid data"));
    }

    @Test
    void updatePortfolio_ShouldReturnInternalServerErrorOnException() throws Exception {
        when(jwtService.extractUsername("valid.token")).thenReturn("consultant@example.com");
        when(userRepository.findByEmail("consultant@example.com")).thenReturn(Optional.of(testConsultant));
        when(portfolioService.saveOrUpdatePortfolio(eq(testConsultant), any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/api/user/portfolio")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getCompletedPortfolios_ShouldReturnPortfoliosList() throws Exception {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        when(portfolioService.getCompletedPortfolios()).thenReturn(List.of(portfolio));

        mockMvc.perform(get("/api/user/consultants/completed"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(portfolioService).getCompletedPortfolios();
    }

    @Test
    void getConsultantsNearLocation_ShouldReturnConsultantsList() throws Exception {
        ConsultantListResponse response = ConsultantListResponse.builder()
                .consultants(List.of())
                .pagination(ConsultantListResponse.Pagination.builder()
                        .total(0)
                        .page(0)
                        .size(10)
                        .build())
                .build();

        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(consultantService.getConsultantsNearLocation(any(), any(), anyInt(), anyInt(), any(), anyString(), anyString())).thenReturn(response);

        mockMvc.perform(get("/api/user/consultants")
                        .param("lat", "1.0")
                        .param("long", "2.0")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(10));

        verify(jwtService).validateToken("valid.token");
        verify(consultantService).getConsultantsNearLocation(any(), any(), eq(0), eq(10), any(), anyString(), anyString());
    }

    @Test
    void getConsultantsNearLocation_ShouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        when(jwtService.validateToken("invalid.token")).thenReturn(false);
        ResponseEntity<ApiErrorResponse> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        when(apiErrorBuilder.error(any(HttpStatus.class), anyString(), anyString())).thenReturn(responseEntity);

        mockMvc.perform(get("/api/user/consultants")
                        .param("lat", "1.0")
                        .param("long", "2.0")
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());

        verify(jwtService).validateToken("invalid.token");
        verify(apiErrorBuilder).error(eq(HttpStatus.UNAUTHORIZED), eq("UNAUTHORIZED"), eq("Unauthorized"));
        verify(consultantService, never()).getConsultantsNearLocation(any(), any(), anyInt(), anyInt(), any(), anyString(), anyString());
    }

    @Test
    void getConsultantsNearLocation_ShouldReturnBadRequestOnInvalidCoordinates() throws Exception {
        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(consultantService.getConsultantsNearLocation(any(), any(), anyInt(), anyInt(), any(), anyString(), anyString())).thenThrow(new IllegalArgumentException("Invalid coordinates"));

        mockMvc.perform(get("/api/user/consultants")
                        .param("lat", "invalid")
                        .param("long", "2.0")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isBadRequest());

        verify(consultantService).getConsultantsNearLocation(any(), any(), anyInt(), anyInt(), any(), anyString(), anyString());
    }

    @Test
    void getConsultantsNearLocation_ShouldReturnBadRequestOnInvalidConsultantType() throws Exception {
        when(jwtService.validateToken("valid.token")).thenReturn(true);

        mockMvc.perform(get("/api/user/consultants")
                        .param("lat", "1.0")
                        .param("long", "2.0")
                        .param("type", "invalid")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isBadRequest());

        verify(consultantService, never()).getConsultantsNearLocation(any(), any(), anyInt(), anyInt(), any(), anyString(), anyString());
    }

    @Test
    void getConsultantsNearLocation_ShouldReturnInternalServerErrorOnException() throws Exception {
        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(consultantService.getConsultantsNearLocation(any(), any(), anyInt(), anyInt(), any(), anyString(), anyString())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/user/consultants")
                        .param("lat", "1.0")
                        .param("long", "2.0")
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addPortfolioProject_ShouldAddProjectSuccessfully() throws Exception {
        PortfolioProjectRequest projectRequest = new PortfolioProjectRequest();
        projectRequest.setProjectName("New Project");
        projectRequest.setPrice(BigDecimal.valueOf(1000.00));
        projectRequest.setDetailDescription("Project description");
        projectRequest.setImageUrls(List.of("image1.jpg"));

        when(jwtService.extractUsername("valid.token")).thenReturn("consultant@example.com");
        when(userRepository.findByEmail("consultant@example.com")).thenReturn(Optional.of(testConsultant));
        when(portfolioService.addProjectToPortfolio(eq(testConsultant), any())).thenReturn(new com.hub.app.user.model.PortfolioProject());

        mockMvc.perform(post("/api/user/portfolio/projects")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isCreated());

        verify(portfolioService).addProjectToPortfolio(eq(testConsultant), any());
    }

    @Test
    void addPortfolioProject_ShouldReturnBadRequestWhenUserNotConsultant() throws Exception {
        PortfolioProjectRequest projectRequest = new PortfolioProjectRequest();
        projectRequest.setProjectName("New Project");

        when(jwtService.extractUsername("valid.token")).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/user/portfolio/projects")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isBadRequest());

        verify(portfolioService, never()).addProjectToPortfolio(any(), any());
    }

    @Test
    void addPortfolioProject_ShouldReturnBadRequestOnDomainException() throws Exception {
        PortfolioProjectRequest projectRequest = new PortfolioProjectRequest();
        projectRequest.setProjectName("New Project");

        when(jwtService.extractUsername("valid.token")).thenReturn("consultant@example.com");
        when(userRepository.findByEmail("consultant@example.com")).thenReturn(Optional.of(testConsultant));
        when(portfolioService.addProjectToPortfolio(eq(testConsultant), any())).thenThrow(new DomainException(ErrorCode.BAD_REQUEST, "Invalid data"));

        ResponseEntity<ApiErrorResponse> responseEntity = ResponseEntity.badRequest().build();
        when(apiErrorBuilder.error(any(HttpStatus.class), anyString(), anyString())).thenReturn(responseEntity);

        mockMvc.perform(post("/api/user/portfolio/projects")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isBadRequest());

        verify(apiErrorBuilder).error(eq(HttpStatus.BAD_REQUEST), eq("BAD_REQUEST"), eq("Invalid data"));
    }

    @Test
    void addPortfolioProject_ShouldReturnInternalServerErrorOnException() throws Exception {
        PortfolioProjectRequest projectRequest = new PortfolioProjectRequest();
        projectRequest.setProjectName("New Project");

        when(jwtService.extractUsername("valid.token")).thenReturn("consultant@example.com");
        when(userRepository.findByEmail("consultant@example.com")).thenReturn(Optional.of(testConsultant));
        when(portfolioService.addProjectToPortfolio(eq(testConsultant), any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/user/portfolio/projects")
                        .header("Authorization", "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void uploadPortfolioImages_ShouldUploadImagesSuccessfully() throws Exception {
        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(minioService.uploadFile(any(), anyString(), anyString())).thenReturn("uploaded-url.jpg");

        mockMvc.perform(multipart("/api/user/portfolio/upload-images")
                        .file("files", "test image content".getBytes())
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrls").isArray())
                .andExpect(jsonPath("$.imageUrls[0]").value("uploaded-url.jpg"));

        verify(minioService).uploadFile(any(), eq("hub-files"), anyString());
    }

    @Test
    void uploadPortfolioImages_ShouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        when(jwtService.validateToken("invalid.token")).thenReturn(false);
        ResponseEntity<ApiErrorResponse> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        when(apiErrorBuilder.error(any(HttpStatus.class), anyString(), anyString())).thenReturn(responseEntity);

        mockMvc.perform(multipart("/api/user/portfolio/upload-images")
                        .file("files", "test image content".getBytes())
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());

        verify(jwtService).validateToken("invalid.token");
        verify(apiErrorBuilder).error(eq(HttpStatus.UNAUTHORIZED), eq("UNAUTHORIZED"), eq("Unauthorized"));
        verify(minioService, never()).uploadFile(any(), anyString(), anyString());
    }

    @Test
    void uploadPortfolioImages_ShouldReturnInternalServerErrorOnException() throws Exception {
        when(jwtService.validateToken("valid.token")).thenReturn(true);
        when(minioService.uploadFile(any(), anyString(), anyString())).thenThrow(new RuntimeException("Upload failed"));

        mockMvc.perform(multipart("/api/user/portfolio/upload-images")
                        .file("files", "test image content".getBytes())
                        .header("Authorization", "Bearer valid.token"))
                .andExpect(status().isInternalServerError());
    }
}