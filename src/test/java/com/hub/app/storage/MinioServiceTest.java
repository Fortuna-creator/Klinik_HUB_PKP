package com.hub.app.storage;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioService minioService;

    private MockMultipartFile validImageFile;
    private MockMultipartFile largeFile;
    private MockMultipartFile invalidTypeFile;

    @BeforeEach
    void setUp() throws Exception {
        // Create a small valid JPEG image using actual image creation
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();
        validImageFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", imageBytes);

        // Large file exceeding limit (6MB)
        byte[] largeBytes = new byte[6 * 1024 * 1024];
        largeFile = new MockMultipartFile("file", "large.jpg", "image/jpeg", largeBytes);

        // Invalid file type
        invalidTypeFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
    }

    @Test
    void uploadFile_ShouldUploadSuccessfully_WhenValidFile() throws Exception {
        String fileType = "KTP";

        String result = minioService.uploadFile(validImageFile, fileType);

        assertNotNull(result);
        assertTrue(result.contains("hub-files"));
        assertTrue(result.contains("checksum"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadFile_ShouldThrowException_WhenFileTooLarge() {
        String fileType = "KTP";

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> minioService.uploadFile(largeFile, fileType));

        assertEquals("Error uploading file to MinIO", exception.getMessage());
        assertEquals("File size exceeds 2 MB limit", exception.getCause().getMessage());
    }

    @Test
    void uploadFile_ShouldThrowException_WhenInvalidFileType() {
        String fileType = "KTP";

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> minioService.uploadFile(invalidTypeFile, fileType));

        assertEquals("Error uploading file to MinIO", exception.getMessage());
        assertEquals("Only JPEG and PNG image files are allowed", exception.getCause().getMessage());
    }

    @Test
    void downloadFile_ShouldReturnInputStream_WhenFileExists() throws Exception {
        String fileName = "test.jpg";
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        InputStream result = minioService.downloadFile(fileName);

        assertNotNull(result);
        assertEquals(mockResponse, result);
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
    }

    @Test
    void downloadFile_ShouldThrowException_WhenFileNotFound() throws Exception {
        String fileName = "nonexistent.jpg";
        when(minioClient.getObject(any(GetObjectArgs.class)))
            .thenThrow(new RuntimeException("Object not found"));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> minioService.downloadFile(fileName));

        assertEquals("Error downloading file from MinIO", exception.getMessage());
    }
}