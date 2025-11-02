package com.hub.app.file.service;

import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.file.dto.FileDownloadResponse;
import com.hub.app.file.dto.FileListResponse;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final FileUploadRepository fileUploadRepository;
    private final MinioService minioService;

    @Transactional
    public FileUploadResponse uploadFile(String category, String subCategory, String entityId, MultipartFile file) {
        // Resolve bucket and path
        String bucketName = BucketResolver.resolveBucket();
        String pathPrefix = BucketResolver.resolvePath(category, subCategory, entityId);

        // Generate unique file name
        String originalFileName = file.getOriginalFilename();
        String fileName = FileNameGenerator.generateFileName(entityId, originalFileName);
        String objectPath = pathPrefix + "/" + fileName;

        // Upload to MinIO
        String fileUrl = minioService.uploadFile(file, bucketName, objectPath);

        // Save metadata to database
        FileUpload fileUpload = new FileUpload();
        fileUpload.setUserId(entityId); // using entityId as userId for now
        fileUpload.setUsecase(category + (subCategory != null ? "/" + subCategory : ""));
        fileUpload.setBucketName(bucketName);
        fileUpload.setObjectPath(objectPath);
        fileUpload.setOriginalName(originalFileName);
        fileUpload.setMimeType(file.getContentType());
        fileUpload.setCategory(category);
        fileUpload.setSubCategory(subCategory);
        fileUpload.setEntityId(entityId);

        FileUpload savedFile = fileUploadRepository.save(fileUpload);

        log.info("File uploaded successfully: {}", savedFile.getFileId());

        return new FileUploadResponse("Upload success", fileUrl, savedFile.getFileId());
    }

    public FileDownloadResponse downloadFile(String path) {
        String bucketName = BucketResolver.resolveBucket();
        String presignedUrl = minioService.generatePresignedUrl(bucketName, path);
        return new FileDownloadResponse(presignedUrl);
    }

    public FileListResponse listFiles(String category, String entityId) {
        String bucketName = BucketResolver.resolveBucket();
        String pathPrefix = BucketResolver.resolvePath(category, null, entityId);

        List<String> objectPaths = minioService.listObjects(bucketName, pathPrefix);

        // Get metadata from database for each file
        List<FileListResponse.FileInfo> files = objectPaths.stream()
                .map(objectPath -> {
                    Optional<FileUpload> metadata = fileUploadRepository.findByObjectPath(objectPath);
                    if (metadata.isPresent()) {
                        FileUpload file = metadata.get();
                        String presignedUrl = minioService.generatePresignedUrl(bucketName, objectPath);
                        return new FileListResponse.FileInfo(
                                file.getOriginalName(),
                                presignedUrl,
                                file.getUserId(),
                                file.getUploadedAt()
                        );
                    } else {
                        // Fallback if metadata not found
                        String fileName = objectPath.substring(objectPath.lastIndexOf('/') + 1);
                        String presignedUrl = minioService.generatePresignedUrl(bucketName, objectPath);
                        return new FileListResponse.FileInfo(
                                fileName,
                                presignedUrl,
                                "unknown",
                                null
                        );
                    }
                })
                .collect(Collectors.toList());

        return new FileListResponse(files);
    }

    public Optional<FileUpload> getFileMetadata(UUID fileId) {
        return fileUploadRepository.findByFileId(fileId);
    }
}