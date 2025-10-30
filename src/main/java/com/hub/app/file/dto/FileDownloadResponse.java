package com.hub.app.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDownloadResponse {
    private String fileUrl;
}