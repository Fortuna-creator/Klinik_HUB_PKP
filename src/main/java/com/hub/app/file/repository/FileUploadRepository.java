package com.hub.app.file.repository;

import com.hub.app.file.model.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, UUID> {

    Optional<FileUpload> findByFileId(UUID fileId);

    Optional<FileUpload> findByObjectPath(String objectPath);

    List<FileUpload> findByUserId(String userId);

    List<FileUpload> findByUserIdAndUsecase(String userId, String usecase);

    boolean existsByObjectPath(String objectPath);
}