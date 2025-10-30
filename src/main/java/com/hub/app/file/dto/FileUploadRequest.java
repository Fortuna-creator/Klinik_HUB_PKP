package com.hub.app.file.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadRequest {
    private String category; // verification, contracts, projects, design-prototypes, system
    private String subCategory; // ktp, selfie, ded, rab, boq, draft, signed (optional)
    private String entityId; // homeOwnerId, projectId, designId
    private MultipartFile file;
}