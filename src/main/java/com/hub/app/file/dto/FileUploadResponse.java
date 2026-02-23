package com.hub.app.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private String message;
    private String fileUrl;
    private UUID fileId;
}