package com.hub.app.file.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadRequest {
    private MultipartFile file;
    private String fileType; // KTP, Document, Selfie, etc.
}