package com.hub.app.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName = "hub-files";

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB

    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 🔍 Validasi ukuran file
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("File size exceeds 2 MB limit");
            }

            // 🔍 Hitung checksum SHA-256
            String checksum = calculateChecksum(file.getInputStream(), "SHA-256");

            // Generate unique filename
            String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            // Upload ke MinIO
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            // Return URL + checksum untuk verifikasi
            return "/" + bucketName + "/" + fileName + "?checksum=" + checksum;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    private String calculateChecksum(InputStream inputStream, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] block = new byte[4096];
        int length;
        while ((length = inputStream.read(block)) > 0) {
            digest.update(block, 0, length);
        }
        return Base64.getEncoder().encodeToString(digest.digest());
    }
}
