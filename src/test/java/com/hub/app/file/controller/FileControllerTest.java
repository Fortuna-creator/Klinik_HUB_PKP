package com.hub.app.file.controller;

import com.hub.app.auth.service.JwtService;
import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.file.model.FileUpload;
import com.hub.app.file.service.FileUploadService;
import com.hub.app.storage.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MinioService minioService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private FileUploadService fileUploadService;

    @Test
    void uploadFile_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String usecase = "PHOTO_KTP";
        String userId = "USR12345";
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        FileUploadResponse expectedResponse = new FileUploadResponse(
                "123e4567-e89b-12d3-a456-426614174000",
                "user-identity",
                "USR12345/PHOTO_KTP/20251022T2015_ab12cd34_test.jpg",
                "test.jpg",
                "image/jpeg"
        );
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(fileUploadService.uploadFile(usecase, userId, file)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("usecase", usecase)
                .param("userId", userId)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value("123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(jsonPath("$.bucketName").value("user-identity"))
                .andExpect(jsonPath("$.objectPath").value("USR12345/PHOTO_KTP/20251022T2015_ab12cd34_test.jpg"))
                .andExpect(jsonPath("$.originalName").value("test.jpg"))
                .andExpect(jsonPath("$.mimeType").value("image/jpeg"));
    }

    @Test
    void uploadFile_ShouldReturnUnauthorized_WhenInvalidToken() throws Exception {
        // Arrange
        String invalidToken = "invalid.token";
        String usecase = "PHOTO_KTP";
        String userId = "USR12345";
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test content".getBytes());
        when(jwtService.validateToken(invalidToken)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("usecase", usecase)
                .param("userId", userId)
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void uploadFile_ShouldReturnBadRequest_WhenInvalidUsecase() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String invalidUsecase = "INVALID_USECASE";
        String userId = "USR12345";
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test content".getBytes());
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(fileUploadService.uploadFile(invalidUsecase, userId, file))
                .thenThrow(new IllegalArgumentException("Unsupported usecase: " + invalidUsecase));

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("usecase", invalidUsecase)
                .param("userId", userId)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unsupported usecase: INVALID_USECASE"));
    }

    @Test
    void downloadFile_ShouldReturnFile_WhenValidRequest() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String fileId = "123e4567-e89b-12d3-a456-426614174000";
        FileUpload fileUpload = new FileUpload();
        fileUpload.setBucketName("user-identity");
        fileUpload.setObjectPath("USR12345/PHOTO_KTP/test.jpg");
        fileUpload.setOriginalName("test.jpg");
        fileUpload.setMimeType("image/jpeg");

        ByteArrayInputStream fileStream = new ByteArrayInputStream("file content".getBytes());
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(fileUploadService.getFileMetadata(UUID.fromString(fileId))).thenReturn(Optional.of(fileUpload));
        when(minioService.downloadFile("user-identity", "USR12345/PHOTO_KTP/test.jpg")).thenReturn(fileStream);

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
        String fileId = "123e4567-e89b-12d3-a456-426614174000";
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
        String fileId = "123e4567-e89b-12d3-a456-426614174000";
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(fileUploadService.getFileMetadata(UUID.fromString(fileId))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/files/download/{fileId}", fileId)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("File not found"));
    }
}