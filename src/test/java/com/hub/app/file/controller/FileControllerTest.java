package com.hub.app.file.controller;

import com.hub.app.auth.service.JwtService;
import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.storage.MinioService;
import com.hub.app.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MinioService minioService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void uploadFile_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String fileType = "KTP";
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        String expectedUrl = "/hub-files/KTP/uuid-test.jpg?checksum=abc123";
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(minioService.uploadFile(any(), eq(fileType))).thenReturn(expectedUrl);

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("fileType", fileType)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").exists())
                .andExpect(jsonPath("$.fileUrl").value(expectedUrl))
                .andExpect(jsonPath("$.checksum").value("abc123"));
    }

    @Test
    void uploadFile_ShouldReturnUnauthorized_WhenInvalidToken() throws Exception {
        // Arrange
        String invalidToken = "invalid.token";
        String fileType = "KTP";
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test content".getBytes());
        when(jwtService.validateToken(invalidToken)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("fileType", fileType)
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void uploadFile_ShouldReturnBadRequest_WhenInvalidFileType() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String invalidFileType = "INVALID";
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test content".getBytes());
        when(jwtService.validateToken(validToken)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("fileType", invalidFileType)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid file type. Supported types: KTP, Document, Selfie"));
    }

    @Test
    void downloadFile_ShouldReturnFile_WhenValidRequest() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String fileId = "KTP/test.jpg";
        ByteArrayInputStream fileStream = new ByteArrayInputStream("file content".getBytes());
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(minioService.downloadFile(fileId)).thenReturn(fileStream);

        // Act & Assert
        mockMvc.perform(get("/api/files/download/{fileId}", fileId)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.jpg\""));
    }

    @Test
    void downloadFile_ShouldReturnUnauthorized_WhenInvalidToken() throws Exception {
        // Arrange
        String invalidToken = "invalid.token";
        String fileId = "KTP/test.jpg";
        when(jwtService.validateToken(invalidToken)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/files/download/{fileId}", fileId)
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void downloadFile_ShouldReturnNotFound_WhenFileNotFound() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String fileId = "nonexistent.jpg";
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(minioService.downloadFile(fileId)).thenThrow(new RuntimeException("File not found"));

        // Act & Assert
        mockMvc.perform(get("/api/files/download/{fileId}", fileId)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("File not found"));
    }
}