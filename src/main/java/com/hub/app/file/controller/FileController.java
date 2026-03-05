package com.hub.app.file.controller;

import com.hub.app.user.repository.UserRepository;
import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.file.dto.FileDownloadResponse;
import com.hub.app.file.dto.FileListResponse;
import com.hub.app.file.model.FileUpload;
import com.hub.app.file.service.FileUploadService;
import com.hub.app.storage.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestPart("category") String category,
            @RequestPart(value = "subCategory", required = false) String subCategory,
            @RequestPart("entityId") String entityId,
            @RequestPart("file") MultipartFile file) {

        try {
            // Authentication is already validated by JwtAuthenticationFilter / Spring Security

            // Validate required parameters
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Category is required");
            }
            if (entityId == null || entityId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("EntityId is required");
            }
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is required");
            }

            // Upload file
            FileUploadResponse response = fileUploadService.uploadFile(category, subCategory, entityId, file);

            log.info("File uploaded successfully for category: {}, entityId: {}", category, entityId);
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
            @PathVariable UUID fileId,
            Authentication authentication) {

        try {
            // Authentication is already validated by JwtAuthenticationFilter / Spring Security
            String email = authentication.getName();
            Long userId = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found")).getId();

            log.info("File download request: fileId={}, userId={}", fileId, userId);

            // Get file metadata and check access
            Optional<FileUpload> fileMetadata = fileUploadService.getFileMetadata(fileId);
            if (fileMetadata.isEmpty()) {
                log.warn("File not found for download: fileId={}", fileId);
                return ResponseEntity.status(404).body("File not found");
            }

            FileUpload fileUpload = fileMetadata.get();
            log.debug("File metadata retrieved: fileId={}, category={}, entityId={}, originalName={}",
                     fileId, fileUpload.getCategory(), fileUpload.getEntityId(), fileUpload.getOriginalName());

            // Check user access to this file (business logic validation)
            boolean hasAccess = fileUploadService.checkUserAccessToFile(userId, fileUpload);
            if (!hasAccess) {
                log.warn("Access denied for file download: fileId={}, userId={}, category={}, entityId={}",
                        fileId, userId, fileUpload.getCategory(), fileUpload.getEntityId());
                return ResponseEntity.status(403).body("Access denied");
            }

            log.debug("Access granted, streaming file from MinIO: bucket={}, objectPath={}",
                     fileUpload.getBucketName(), fileUpload.getObjectPath());

            // Stream file from MinIO
            InputStream inputStream = minioService.downloadFile(fileUpload.getBucketName(), fileUpload.getObjectPath());

            log.info("File download successful: fileId={}, userId={}, fileName={}",
                    fileId, userId, fileUpload.getOriginalName());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileUpload.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                           "attachment; filename=\"" + fileUpload.getOriginalName() + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("File download error for fileId: {}, userId: {}", fileId, "unknown", e);
            return ResponseEntity.status(500).body("File download failed");
        }
    }


    // Keep the old presigned URL method for backward compatibility
    @GetMapping("/download-url")
    public ResponseEntity<?> downloadFileByPath(
            @RequestParam("path") String path) {

        try {
            // Authentication is already validated by JwtAuthenticationFilter / Spring Security

            // Generate presigned URL for download
            FileDownloadResponse response = fileUploadService.downloadFile(path);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("File download error for path: {}", path, e);
            return ResponseEntity.status(404).body("File not found");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listFiles(
            @RequestParam("category") String category,
            @RequestParam("entityId") String entityId) {

        try {
            // Authentication is already validated by JwtAuthenticationFilter / Spring Security

            // List files
            FileListResponse response = fileUploadService.listFiles(category, entityId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("File list error for category: {}, entityId: {}", category, entityId, e);
            return ResponseEntity.internalServerError().body("An error occurred while listing files");
        }
    }

}