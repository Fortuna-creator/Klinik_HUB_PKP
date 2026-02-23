package com.hub.app.file.controller;

import com.hub.app.auth.service.JwtService;
import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.file.dto.FileDownloadResponse;
import com.hub.app.file.dto.FileListResponse;
import com.hub.app.file.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileUploadService fileUploadService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        when(jwtService.validateToken(anyString())).thenReturn(true);
    }

    @Test
    void uploadFile_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());
        FileUploadResponse expectedResponse = new FileUploadResponse("Upload success", "http://minio-endpoint/hub-documents/test.pdf", java.util.UUID.randomUUID());

        when(fileUploadService.uploadFile("verification", "ktp", "user123", file)).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("category", "verification")
                .param("subCategory", "ktp")
                .param("entityId", "user123")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Upload success"))
                .andExpect(jsonPath("$.fileUrl").exists());
    }

    @Test
    void uploadFile_InvalidToken() throws Exception {
        // Given
        when(jwtService.validateToken(anyString())).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());

        // When & Then
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("category", "verification")
                .param("entityId", "user123")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void downloadFile_Success() throws Exception {
        // Given
        FileDownloadResponse expectedResponse = new FileDownloadResponse("https://minio-endpoint/hub-documents/test.pdf?signature=...");
        when(fileUploadService.downloadFile("verification/user123/ktp/test.pdf")).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/files/download")
                .param("path", "verification/user123/ktp/test.pdf")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").exists());
    }

    @Test
    void listFiles_Success() throws Exception {
        // Given
        FileListResponse.FileInfo fileInfo = new FileListResponse.FileInfo("test.pdf", "https://minio-endpoint/test.pdf", "user123", null);
        FileListResponse expectedResponse = new FileListResponse(java.util.List.of(fileInfo));

        when(fileUploadService.listFiles("verification", "user123")).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/files/list")
                .param("category", "verification")
                .param("entityId", "user123")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.files").isArray());
    }
}