package com.hub.app.file.controller;

import com.hub.app.auth.service.JwtService;
import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.storage.MinioService;
import com.hub.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final MinioService minioService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("file") MultipartFile file,
            @RequestParam("fileType") String fileType) {

        try {
            // Validate JWT token
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(401).body("Invalid token");
            }

            // Validate file type
            if (!isValidFileType(fileType)) {
                return ResponseEntity.badRequest().body("Invalid file type. Supported types: KTP, Document, Selfie");
            }

            // Upload file
            String fileUrl = minioService.uploadFile(file, fileType);

            // Extract fileId from URL (remove leading slash and checksum)
            String fileId = extractFileIdFromUrl(fileUrl);

            // Extract checksum from URL
            String checksum = extractChecksumFromUrl(fileUrl);

            FileUploadResponse response = new FileUploadResponse(fileId, fileUrl, checksum);

            log.info("File uploaded successfully: {}", fileId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("File upload validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("File upload error", e);
            return ResponseEntity.internalServerError().body("An error occurred during file upload");
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String fileId) {

        try {
            // Validate JWT token
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(401).body("Invalid token");
            }

            // Download file
            InputStream inputStream = minioService.downloadFile(fileId);

            // Determine content type based on file extension
            String contentType = determineContentType(fileId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + extractFileName(fileId) + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("File download error for fileId: {}", fileId, e);
            return ResponseEntity.status(404).body("File not found");
        }
    }

    private boolean isValidFileType(String fileType) {
        return fileType != null && (fileType.equals("KTP") || fileType.equals("Document") || fileType.equals("Selfie"));
    }

    private String extractFileIdFromUrl(String fileUrl) {
        // Remove leading slash and everything after ?
        int questionMarkIndex = fileUrl.indexOf('?');
        String path = questionMarkIndex > 0 ? fileUrl.substring(1, questionMarkIndex) : fileUrl.substring(1);
        // Remove bucket name prefix
        return path.replaceFirst("hub-files/", "");
    }

    private String extractChecksumFromUrl(String fileUrl) {
        int checksumIndex = fileUrl.indexOf("checksum=");
        return checksumIndex > 0 ? fileUrl.substring(checksumIndex + 9) : "";
    }

    private String determineContentType(String fileId) {
        String fileName = extractFileName(fileId).toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    private String extractFileName(String fileId) {
        int lastSlashIndex = fileId.lastIndexOf('/');
        return lastSlashIndex > 0 ? fileId.substring(lastSlashIndex + 1) : fileId;
    }
}