package com.hub.app.file.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadRequest {
    private String usecase; // PHOTO_KTP, PHOTO_SELFIE, DOCUMENT_DED, etc.
    private String userId;
    private MultipartFile file;
}