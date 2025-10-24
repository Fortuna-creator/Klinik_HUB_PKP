package com.hub.app.storage;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName = "hub-files";
    private final String endpoint = "http://localhost:9000";

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public String uploadFile(MultipartFile file, String bucketName, String objectPath) {
        try {
            // 🔍 Validasi ukuran file
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("File size exceeds 5 MB limit");
            }

            // 🔍 Validasi tipe file untuk gambar (JPEG, PNG) - bisa diperluas untuk dokumen
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") &&
                !contentType.equals("application/pdf") && !contentType.startsWith("application/vnd"))) {
                throw new RuntimeException("Unsupported file type. Allowed: JPEG, PNG, PDF, Excel, Word");
            }

            // 🔍 Compress image if needed (only for images)
            byte[] fileBytes;
            if (contentType.startsWith("image/")) {
                fileBytes = compressImageIfNeeded(file);
            } else {
                fileBytes = file.getBytes();
            }

            // Upload ke MinIO dengan bucket dan path yang ditentukan
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
            throw new RuntimeException("Error downloading file from MinIO", e);
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
