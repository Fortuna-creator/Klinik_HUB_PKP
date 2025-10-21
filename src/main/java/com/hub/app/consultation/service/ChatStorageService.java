package com.hub.app.consultation.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatStorageService {

    private final MinioClient minioClient;
    private final String bucketName = "hub-files";

    private static final long MAX_VOICE_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    public String uploadVoiceNote(MultipartFile file) {
        try {
            // Validate file size
            if (file.getSize() > MAX_VOICE_FILE_SIZE) {
                throw new RuntimeException("Voice note file size exceeds 10 MB limit");
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("audio/mpeg") && !contentType.equals("audio/mp4"))) {
                throw new RuntimeException("Only MP3 and M4A audio files are allowed");
            }

            // Generate unique filename
            String fileName = "chat/voice/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            // Upload to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            // Return URL
            return "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading voice note to MinIO", e);
        }
    }
}