package com.hub.app.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private String fileId;
    private String fileUrl;
    private String checksum;
}