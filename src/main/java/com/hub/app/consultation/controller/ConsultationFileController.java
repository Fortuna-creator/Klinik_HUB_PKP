package com.hub.app.consultation.controller;

import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.consultation.service.ConsultationFileService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.file.model.FileUpload;
import com.hub.app.storage.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/consultations/{consultationId}/files")
@RequiredArgsConstructor
@Slf4j
public class ConsultationFileController {

    private final ConsultationFileService consultationFileService;
    private final ConsultationJwtService jwtService;
    private final MinioService minioService;
    private final ApiErrorBuilder apiErrorBuilder;

    // ========== CONTRACT FILES ==========

    @PostMapping(value = "/contract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> uploadContractFile(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("file") MultipartFile file) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            FileUploadResponse response = consultationFileService.uploadContractFile(consultationId, userId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Contract file upload error", e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/contract")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getContractFiles(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            List<FileUpload> files = consultationFileService.getContractFiles(consultationId, userId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/contract/{fileId}/download")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> downloadContractFile(
            @PathVariable UUID consultationId,
            @PathVariable UUID fileId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            Optional<FileUpload> fileOpt = consultationFileService.getConsultationFile(consultationId, userId, fileId);

            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            FileUpload fileUpload = fileOpt.get();
            InputStream inputStream = minioService.downloadFile(fileUpload.getBucketName(), fileUpload.getObjectPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileUpload.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileUpload.getOriginalName() + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("Contract file download error", e);
            return ResponseEntity.status(404).body("File not found");
        }
    }

    // ========== REVISION FILES ==========

    @PostMapping(value = "/revision", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> uploadRevisionFile(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("file") MultipartFile file) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            FileUploadResponse response = consultationFileService.uploadRevisionFile(consultationId, userId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Revision file upload error", e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/revision")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getRevisionFiles(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            List<FileUpload> files = consultationFileService.getRevisionFiles(consultationId, userId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/revision/{fileId}/download")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> downloadRevisionFile(
            @PathVariable UUID consultationId,
            @PathVariable UUID fileId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            Optional<FileUpload> fileOpt = consultationFileService.getConsultationFile(consultationId, userId, fileId);

            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            FileUpload fileUpload = fileOpt.get();
            InputStream inputStream = minioService.downloadFile(fileUpload.getBucketName(), fileUpload.getObjectPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileUpload.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileUpload.getOriginalName() + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("Revision file download error", e);
            return ResponseEntity.status(404).body("File not found");
        }
    }

    // ========== CHAT FILES ==========

    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> uploadChatFile(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("file") MultipartFile file) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            FileUploadResponse response = consultationFileService.uploadChatFile(consultationId, userId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Chat file upload error", e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/chat")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getChatFiles(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            List<FileUpload> files = consultationFileService.getChatFiles(consultationId, userId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/chat/{fileId}/download")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> downloadChatFile(
            @PathVariable UUID consultationId,
            @PathVariable UUID fileId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            Optional<FileUpload> fileOpt = consultationFileService.getConsultationFile(consultationId, userId, fileId);

            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            FileUpload fileUpload = fileOpt.get();
            InputStream inputStream = minioService.downloadFile(fileUpload.getBucketName(), fileUpload.getObjectPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileUpload.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileUpload.getOriginalName() + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("Chat file download error", e);
            return ResponseEntity.status(404).body("File not found");
        }
    }

    // ========== SURVEY PHOTOS ==========

    @PostMapping(value = "/survey", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> uploadSurveyPhoto(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("file") MultipartFile file) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            FileUploadResponse response = consultationFileService.uploadSurveyPhoto(consultationId, userId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Survey photo upload error", e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/survey")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getSurveyPhotos(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            List<FileUpload> files = consultationFileService.getSurveyPhotos(consultationId, userId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/survey/{fileId}/download")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> downloadSurveyPhoto(
            @PathVariable UUID consultationId,
            @PathVariable UUID fileId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            Optional<FileUpload> fileOpt = consultationFileService.getConsultationFile(consultationId, userId, fileId);

            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            FileUpload fileUpload = fileOpt.get();
            InputStream inputStream = minioService.downloadFile(fileUpload.getBucketName(), fileUpload.getObjectPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileUpload.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileUpload.getOriginalName() + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("Survey photo download error", e);
            return ResponseEntity.status(404).body("File not found");
        }
    }

    // ========== FINAL DOCUMENTS ==========

    @PostMapping(value = "/final", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> uploadFinalDocument(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("file") MultipartFile file) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            FileUploadResponse response = consultationFileService.uploadFinalDocument(consultationId, userId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Final document upload error", e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/final")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getFinalDocuments(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            List<FileUpload> files = consultationFileService.getFinalDocuments(consultationId, userId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/final/{fileId}/download")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> downloadFinalDocument(
            @PathVariable UUID consultationId,
            @PathVariable UUID fileId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            Optional<FileUpload> fileOpt = consultationFileService.getConsultationFile(consultationId, userId, fileId);

            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            FileUpload fileUpload = fileOpt.get();
            InputStream inputStream = minioService.downloadFile(fileUpload.getBucketName(), fileUpload.getObjectPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileUpload.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileUpload.getOriginalName() + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("Final document download error", e);
            return ResponseEntity.status(404).body("File not found");
        }
    }

    // ========== DESIGN FILES ==========

    @PostMapping(value = "/design", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> uploadDesignFile(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("file") MultipartFile file) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            FileUploadResponse response = consultationFileService.uploadDesignFile(consultationId, userId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Design file upload error", e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/design")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getDesignFiles(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            List<FileUpload> files = consultationFileService.getDesignFiles(consultationId, userId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }

    @GetMapping("/design/{fileId}/download")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> downloadDesignFile(
            @PathVariable UUID consultationId,
            @PathVariable UUID fileId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            Optional<FileUpload> fileOpt = consultationFileService.getConsultationFile(consultationId, userId, fileId);

            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            FileUpload fileUpload = fileOpt.get();
            InputStream inputStream = minioService.downloadFile(fileUpload.getBucketName(), fileUpload.getObjectPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileUpload.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileUpload.getOriginalName() + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("Design file download error", e);
            return ResponseEntity.status(404).body("File not found");
        }
    }

    // ========== DELETE FILE ==========

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> deleteConsultationFile(
            @PathVariable UUID consultationId,
            @PathVariable UUID fileId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtService.extractUserId(authHeader);
            consultationFileService.deleteConsultationFile(consultationId, userId, fileId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("File deletion error", e);
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                         ErrorCode.INTERNAL_ERROR.getCode(),
                                         e.getMessage());
        }
    }
}