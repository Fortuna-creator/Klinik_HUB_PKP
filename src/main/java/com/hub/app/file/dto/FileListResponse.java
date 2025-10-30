package com.hub.app.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class FileListResponse {
    private List<FileInfo> files;

    @Data
    @AllArgsConstructor
    public static class FileInfo {
        private String name;
        private String url;
        private String uploadedBy;
        private LocalDateTime uploadedAt;
    }
}