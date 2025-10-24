package com.hub.app.file.service;

import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.file.model.FileUpload;
import com.hub.app.file.repository.FileUploadRepository;
import com.hub.app.file.util.BucketResolver;
import com.hub.app.file.util.FileNameGenerator;
import com.hub.app.storage.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final FileUploadRepository fileUploadRepository;
    private final MinioService minioService;

    @Transactional
    public FileUploadResponse uploadFile(String usecase, String userId, MultipartFile file) {
        // Resolve bucket from usecase
        String bucketName = BucketResolver.resolveBucket(usecase);

        // Generate unique file name
        String originalFileName = file.getOriginalFilename();
        String objectPath = FileNameGenerator.generateFileName(userId, usecase, originalFileName);

        // Upload to MinIO
        minioService.uploadFile(file, bucketName, objectPath);

        // Save metadata to database
        FileUpload fileUpload = new FileUpload();
        fileUpload.setUserId(userId);
        fileUpload.setUsecase(usecase);
        fileUpload.setBucketName(bucketName);
        fileUpload.setObjectPath(objectPath);
        fileUpload.setOriginalName(originalFileName);
        fileUpload.setMimeType(file.getContentType());

        FileUpload savedFile = fileUploadRepository.save(fileUpload);

        log.info("File uploaded successfully: {}", savedFile.getFileId());

        return new FileUploadResponse(
                savedFile.getFileId().toString(),
                savedFile.getBucketName(),
                savedFile.getObjectPath(),
                savedFile.getOriginalName(),
                savedFile.getMimeType()
        );
    }

    public Optional<FileUpload> getFileMetadata(UUID fileId) {
        return fileUploadRepository.findByFileId(fileId);
    }
}