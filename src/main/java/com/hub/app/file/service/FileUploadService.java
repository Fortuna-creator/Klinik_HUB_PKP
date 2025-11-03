package com.hub.app.file.service;

import com.hub.app.consultation.repository.ContractRepository;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.file.dto.FileDownloadResponse;
import com.hub.app.file.dto.FileListResponse;
import com.hub.app.file.model.FileUpload;
import com.hub.app.file.repository.FileUploadRepository;
import com.hub.app.file.util.BucketResolver;
import com.hub.app.file.util.FileNameGenerator;
import com.hub.app.storage.MinioService;
import com.hub.app.user.repository.UserRepository;
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
    private final ConsultationRepository consultationRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

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

    public boolean checkUserAccessToFile(Long userId, FileUpload fileUpload) {
        try {
            log.debug("Checking access for userId: {}, fileId: {}, category: {}, entityId: {}",
                     userId, fileUpload.getFileId(), fileUpload.getCategory(), fileUpload.getEntityId());

            // For consultation files, check if user is home owner or consultant
            if ("CONSULTATION".equals(fileUpload.getCategory())) {
                UUID consultationId = UUID.fromString(fileUpload.getEntityId());
                var consultation = consultationRepository.findById(consultationId);
                if (consultation.isPresent()) {
                    var cons = consultation.get();
                    boolean hasAccess = cons.getHomeOwner().getId().equals(userId) ||
                                       cons.getConsultant().getId().equals(userId);
                    log.debug("Consultation access check: userId={}, homeOwnerId={}, consultantId={}, hasAccess={}",
                             userId, cons.getHomeOwner().getId(), cons.getConsultant().getId(), hasAccess);
                    return hasAccess;
                } else {
                    log.warn("Consultation not found for id: {}", consultationId);
                }
            }

            // For contract files, check if user is home owner or consultant of the related consultation
            if ("contracts".equals(fileUpload.getCategory())) {
                try {
                    UUID contractId = UUID.fromString(fileUpload.getEntityId());
                    log.debug("Checking contract access for contractId: {}, userId: {}", contractId, userId);

                    var contract = contractRepository.findById(contractId);
                    if (contract.isPresent()) {
                        var con = contract.get();
                        Long homeOwnerId = con.getConsultation().getHomeOwner().getId();
                        Long consultantId = con.getConsultation().getConsultant().getId();

                        boolean hasAccess = homeOwnerId.equals(userId) || consultantId.equals(userId);
                        log.debug("Contract access check: userId={}, homeOwnerId={}, consultantId={}, hasAccess={}",
                                 userId, homeOwnerId, consultantId, hasAccess);

                        if (!hasAccess) {
                            log.warn("Access denied: user {} is neither home owner ({}) nor consultant ({}) for contract {}",
                                    userId, homeOwnerId, consultantId, contractId);
                        }

                        return hasAccess;
                    } else {
                        log.warn("Contract not found in database: contractId={}, fileId={}, entityId={}",
                                contractId, fileUpload.getFileId(), fileUpload.getEntityId());
                        return false;
                    }
                } catch (Exception e) {
                    log.error("Error checking contract access for fileId: {}, entityId: {}, userId: {}",
                             fileUpload.getFileId(), fileUpload.getEntityId(), userId, e);
                    return false;
                }
            }

            // For project files, check if user owns the project or is assigned consultant
            if ("PROJECT".equals(fileUpload.getCategory())) {
                // TODO: Implement project file access check
                // This would require checking project ownership and consultation assignments
                log.warn("Project file access check not implemented yet, allowing access for userId: {}", userId);
                return true; // Temporarily allow access for all authenticated users
            }

            log.warn("Unknown file category: {} for fileId: {}", fileUpload.getCategory(), fileUpload.getFileId());
            return false;
        } catch (Exception e) {
            log.error("Error checking file access for userId: {}, fileId: {}", userId, fileUpload.getFileId(), e);
            return false;
        }
    }
}