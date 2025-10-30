package com.hub.app.file.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to resolve bucket names and paths based on category and subCategory.
 */
public class BucketResolver {

    private static final String BUCKET_NAME = "hub-documents";

    private static final Map<String, String> CATEGORY_TO_PATH = new HashMap<>();

    static {
        CATEGORY_TO_PATH.put("verification", "verification");
        CATEGORY_TO_PATH.put("contracts", "contracts");
        CATEGORY_TO_PATH.put("projects", "projects");
        CATEGORY_TO_PATH.put("design-prototypes", "design-prototypes");
        CATEGORY_TO_PATH.put("system", "system/templates");
    }

    /**
     * Always returns the fixed bucket name for hub documents.
     *
     * @return the bucket name
     */
    public static String resolveBucket() {
        return BUCKET_NAME;
    }

    /**
     * Resolves the path prefix based on category, subCategory, and entityId.
     *
     * @param category the category (verification, contracts, projects, design-prototypes, system)
     * @param subCategory the subCategory (optional)
     * @param entityId the entity ID
     * @return the path prefix
     * @throws IllegalArgumentException if category is not supported
     */
    public static String resolvePath(String category, String subCategory, String entityId) {
        String basePath = CATEGORY_TO_PATH.get(category);
        if (basePath == null) {
            throw new IllegalArgumentException("Unsupported category: " + category);
        }

        StringBuilder path = new StringBuilder(basePath);

        if ("verification".equals(category) || "contracts".equals(category) || "projects".equals(category)) {
            path.append("/").append(entityId);
            if (subCategory != null && !subCategory.trim().isEmpty()) {
                path.append("/").append(subCategory);
            }
        } else if ("design-prototypes".equals(category)) {
            path.append("/").append(entityId);
        }
        // system/templates doesn't need entityId

        return path.toString();
    }
}