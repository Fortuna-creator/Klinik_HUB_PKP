package com.hub.app.storage;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket:hub-documents}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long PRESIGNED_URL_EXPIRY = 7 * 24 * 60 * 60; // 7 days in seconds

    public String uploadFile(MultipartFile file, String bucketName, String objectPath) {
        try {
            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("File size exceeds 10 MB limit");
            }

            // Validate file type (images, PDFs, documents)
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/") &&
                !contentType.equals("application/pdf") &&
                !contentType.startsWith("application/vnd") &&
                !contentType.startsWith("application/msword"))) {
                throw new RuntimeException("Unsupported file type. Allowed: Images, PDF, Word, Excel");
            }

            // Compress image if needed (only for images)
            byte[] fileBytes;
            if (contentType.startsWith("image/")) {
                fileBytes = compressImageIfNeeded(file);
            } else {
                fileBytes = file.getBytes();
            }

            // Upload to MinIO
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectPath)
                                .stream(is, fileBytes.length, -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            // Return the file URL
            return endpoint + "/" + bucketName + "/" + objectPath;

        } catch (Exception e) {
            log.error("Error uploading file to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    private byte[] compressImageIfNeeded(MultipartFile file) throws Exception {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new RuntimeException("Invalid image file");
        }

        int maxWidth = 1920;
        int maxHeight = 1080;
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // If image is smaller than max, no compression needed
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return file.getBytes();
        }

        // Calculate new dimensions maintaining aspect ratio
        double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // Resize image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();

        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = file.getContentType().equals("image/png") ? "png" : "jpg";
        ImageIO.write(resizedImage, format, baos);
        return baos.toByteArray();
    }

    public InputStream downloadFile(String bucketName, String objectPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error downloading file from MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Error downloading file from MinIO", e);
        }
    }

    public String generatePresignedUrl(String bucketName, String objectPath) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectPath)
                            .expiry((int) PRESIGNED_URL_EXPIRY, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating presigned URL", e);
        }
    }

    public java.util.List<String> listObjects(String bucketName, String prefix) {
        java.util.List<String> objectPaths = new java.util.ArrayList<>();
        try {
            Iterable<Result<io.minio.messages.Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(false)
                            .build()
            );

            for (Result<io.minio.messages.Item> result : results) {
                io.minio.messages.Item item = result.get();
                objectPaths.add(item.objectName());
            }
        } catch (Exception e) {
            log.error("Error listing objects from MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Error listing objects from MinIO", e);
        }
        return objectPaths;
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
