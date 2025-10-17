package com.hub.app.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.auth.service.JwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.project.dto.CreateProjectRequest;
import com.hub.app.project.dto.CreateProjectResponse;
import com.hub.app.project.model.ProjectType;
import com.hub.app.project.service.ProjectService;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ApiErrorBuilder apiErrorBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private CreateProjectRequest validRequest;
    private CreateProjectResponse expectedResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        validRequest = new CreateProjectRequest();
        validRequest.setName("Test Project");
        validRequest.setLongitude(new BigDecimal("106.84513"));
        validRequest.setLatitude(new BigDecimal("-6.21462"));
        validRequest.setLocationDetail("Jl. Sudirman No.1");
        validRequest.setType(ProjectType.PROTOTYPE);
        validRequest.setLandArea(new BigDecimal("120.5"));
        validRequest.setIncome(new BigDecimal("50000000"));

        expectedResponse = new CreateProjectResponse();
        expectedResponse.setProjectId(UUID.randomUUID());
        expectedResponse.setName("Test Project");
        expectedResponse.setType("PROTOTYPE");
        expectedResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createProject_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        when(jwtService.extractUsername("valid-token")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(projectService.createProject(any(CreateProjectRequest.class), eq(testUser)))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.projectId").exists())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.type").value("PROTOTYPE"));
    }

    @Test
    void createProject_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateProjectRequest invalidRequest = new CreateProjectRequest();
        invalidRequest.setName(""); // Invalid: blank name
        invalidRequest.setLongitude(new BigDecimal("200")); // Invalid: longitude > 180
        invalidRequest.setLatitude(new BigDecimal("100")); // Invalid: latitude > 90
        invalidRequest.setLocationDetail("");
        invalidRequest.setType(ProjectType.PROTOTYPE);
        invalidRequest.setLandArea(new BigDecimal("-10")); // Invalid: negative land area
        invalidRequest.setIncome(new BigDecimal("0")); // Invalid: zero income

        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProject_MissingAuthorization_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
}