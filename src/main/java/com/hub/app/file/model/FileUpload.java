package com.hub.app.file.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_uploads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "file_id", columnDefinition = "UUID")
    private UUID fileId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "usecase", nullable = false)
    private String usecase;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(name = "object_path", nullable = false, unique = true)
    private String objectPath;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "entity_id", nullable = false)
    private String entityId;
}