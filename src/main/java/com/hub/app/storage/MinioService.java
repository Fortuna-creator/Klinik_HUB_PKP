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

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public String uploadFile(MultipartFile file, String fileType) {
        try {
            // 🔍 Validasi ukuran file
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("File size exceeds 2 MB limit");
            }

            // 🔍 Validasi tipe file untuk gambar (JPEG, PNG)
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new RuntimeException("Only JPEG and PNG image files are allowed");
            }

            // 🔍 Compress image if needed
            byte[] imageBytes = compressImageIfNeeded(file);

            // 🔍 Hitung checksum SHA-256 dari compressed image
            String checksum = calculateChecksum(new ByteArrayInputStream(imageBytes), "SHA-256");

            // Generate unique filename
            String fileName = fileType + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            // Upload ke MinIO
            try (InputStream is = new ByteArrayInputStream(imageBytes)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(is, imageBytes.length, -1)
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

    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
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
