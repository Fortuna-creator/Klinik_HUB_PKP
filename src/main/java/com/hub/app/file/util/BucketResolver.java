package com.hub.app.file.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to resolve bucket names based on usecase.
 */
public class BucketResolver {

    private static final Map<String, String> USECASE_TO_BUCKET = new HashMap<>();

    static {
        USECASE_TO_BUCKET.put("PHOTO_KTP", "user-identity");
        USECASE_TO_BUCKET.put("PHOTO_SELFIE", "user-identity");
        USECASE_TO_BUCKET.put("DOCUMENT_DED", "project-ded");
        USECASE_TO_BUCKET.put("DOCUMENT_BOQ", "project-boq");
        USECASE_TO_BUCKET.put("DOCUMENT_RAB", "project-rab");
        USECASE_TO_BUCKET.put("IMAGE_MODEL_HOUSE", "housing-models");
        USECASE_TO_BUCKET.put("IMAGE_HOUSE_PLAN", "housing-layouts");
    }

    /**
     * Resolves the bucket name for the given usecase.
     *
     * @param usecase the usecase string
     * @return the bucket name
     * @throws IllegalArgumentException if usecase is not supported
     */
    public static String resolveBucket(String usecase) {
        String bucket = USECASE_TO_BUCKET.get(usecase);
        if (bucket == null) {
            throw new IllegalArgumentException("Unsupported usecase: " + usecase);
        }
        return bucket;
    }
}