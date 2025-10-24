package com.hub.app.file.controller;

import com.hub.app.auth.service.JwtService;
import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.file.model.FileUpload;
import com.hub.app.file.service.FileUploadService;
import com.hub.app.storage.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileUploadService fileUploadService;
    private final MinioService minioService;
    private final JwtService jwtService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("usecase") String usecase,
            @RequestPart("userId") String userId,
            @RequestPart("file") MultipartFile file) {

        try {
            // Validate JWT token
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(401).body("Invalid token");
            }

            // Validate required parameters
            if (usecase == null || usecase.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Usecase is required");
            }
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("UserId is required");
            }
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is required");
            }

            // Upload file using new service
            FileUploadResponse response = fileUploadService.uploadFile(usecase, userId, file);

            log.info("File uploaded successfully: {}", response.getFileId());
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

            // Get file metadata from database
            Optional<FileUpload> fileUploadOpt = fileUploadService.getFileMetadata(UUID.fromString(fileId));
            if (fileUploadOpt.isEmpty()) {
                return ResponseEntity.status(404).body("File not found");
            }

            FileUpload fileUpload = fileUploadOpt.get();

            // Download file from MinIO using bucket and object path
            InputStream inputStream = minioService.downloadFile(fileUpload.getBucketName(), fileUpload.getObjectPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileUpload.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileUpload.getOriginalName() + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("File download error for fileId: {}", fileId, e);
            return ResponseEntity.status(404).body("File not found");
        }
    }

}