package com.hub.app.file.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class to generate unique file names based on entityId, timestamp, and original file name.
 */
public class FileNameGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Generates a unique file name in the format: {entityId}_{timestamp}.{ext}
     *
     * @param entityId the entity ID (homeOwnerId, projectId, designId)
     * @param originalFileName the original file name
     * @return the generated file name
     */
    public static String generateFileName(String entityId, String originalFileName) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String extension = getFileExtension(originalFileName);
        return String.format("%s_%s.%s", entityId, timestamp, extension);
    }

    /**
     * Extracts file extension from filename.
     *
     * @param filename the filename
     * @return the extension (without dot)
     */
    private static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "bin"; // default extension
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}