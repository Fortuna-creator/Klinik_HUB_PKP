package com.hub.app.file.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class to generate unique file names based on user, usecase, and original file name.
 */
public class FileNameGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Generates a unique file name in the format: userId/usecase/timestamp_UUID_originalFileName
     *
     * @param userId the user ID
     * @param usecase the usecase
     * @param originalFileName the original file name
     * @return the generated file name
     */
    public static String generateFileName(String userId, String usecase, String originalFileName) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String uuid = UUID.randomUUID().toString().substring(0, 8); // Use first 8 characters of UUID
        return String.format("%s/%s/%s_%s_%s", userId, usecase, timestamp, uuid, originalFileName);
    }
}