package com.hub.app.consultation.service;

import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import com.hub.app.file.dto.FileUploadResponse;
import com.hub.app.file.model.FileUpload;
import com.hub.app.file.repository.FileUploadRepository;
import com.hub.app.file.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationFileService {

    private final FileUploadService fileUploadService;
    private final FileUploadRepository fileUploadRepository;
    private final ConsultationRepository consultationRepository;

    /**
     * Upload file untuk kontrak konsultasi
     */
    @Transactional
    public FileUploadResponse uploadContractFile(UUID consultationId, Long userId, MultipartFile file) {
        validateConsultationAccess(consultationId, userId);
        return fileUploadService.uploadFile("CONSULTATION_CONTRACT", userId.toString(), file);
    }

    /**
     * Upload file untuk revisi konsultasi
     */
    @Transactional
    public FileUploadResponse uploadRevisionFile(UUID consultationId, Long userId, MultipartFile file) {
        validateConsultationAccess(consultationId, userId);
        return fileUploadService.uploadFile("CONSULTATION_REVISION", userId.toString(), file);
    }

    /**
     * Upload file untuk chat konsultasi
     */
    @Transactional
    public FileUploadResponse uploadChatFile(UUID consultationId, Long userId, MultipartFile file) {
        validateConsultationAccess(consultationId, userId);
        return fileUploadService.uploadFile("CONSULTATION_CHAT_FILE", userId.toString(), file);
    }

    /**
     * Upload foto survey konsultasi
     */
    @Transactional
    public FileUploadResponse uploadSurveyPhoto(UUID consultationId, Long userId, MultipartFile file) {
        validateConsultationAccess(consultationId, userId);
        return fileUploadService.uploadFile("CONSULTATION_SURVEY_PHOTO", userId.toString(), file);
    }

    /**
     * Upload dokumen final konsultasi
     */
    @Transactional
    public FileUploadResponse uploadFinalDocument(UUID consultationId, Long userId, MultipartFile file) {
        validateConsultationAccess(consultationId, userId);
        return fileUploadService.uploadFile("CONSULTATION_FINAL_DOC", userId.toString(), file);
    }

    /**
     * Upload file desain konsultasi
     */
    @Transactional
    public FileUploadResponse uploadDesignFile(UUID consultationId, Long userId, MultipartFile file) {
        validateConsultationAccess(consultationId, userId);
        return fileUploadService.uploadFile("CONSULTATION_DESIGN_FILE", userId.toString(), file);
    }

    /**
     * Get file metadata dengan validasi akses konsultasi
     */
    public Optional<FileUpload> getConsultationFile(UUID consultationId, Long userId, UUID fileId) {
        validateConsultationAccess(consultationId, userId);
        return fileUploadRepository.findByFileId(fileId);
    }

    /**
     * Get semua file untuk konsultasi tertentu
     */
    public List<FileUpload> getConsultationFiles(UUID consultationId, Long userId, String usecase) {
        validateConsultationAccess(consultationId, userId);
        // Note: Repository doesn't have findByUsecase method, using findAll and filter
        return fileUploadRepository.findAll().stream()
                .filter(file -> usecase.equals(file.getUsecase()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get semua file kontrak untuk konsultasi
     */
    public List<FileUpload> getContractFiles(UUID consultationId, Long userId) {
        return getConsultationFiles(consultationId, userId, "CONSULTATION_CONTRACT");
    }

    /**
     * Get semua file revisi untuk konsultasi
     */
    public List<FileUpload> getRevisionFiles(UUID consultationId, Long userId) {
        return getConsultationFiles(consultationId, userId, "CONSULTATION_REVISION");
    }

    /**
     * Get semua file chat untuk konsultasi
     */
    public List<FileUpload> getChatFiles(UUID consultationId, Long userId) {
        return getConsultationFiles(consultationId, userId, "CONSULTATION_CHAT_FILE");
    }

    /**
     * Get semua foto survey untuk konsultasi
     */
    public List<FileUpload> getSurveyPhotos(UUID consultationId, Long userId) {
        return getConsultationFiles(consultationId, userId, "CONSULTATION_SURVEY_PHOTO");
    }

    /**
     * Get semua dokumen final untuk konsultasi
     */
    public List<FileUpload> getFinalDocuments(UUID consultationId, Long userId) {
        return getConsultationFiles(consultationId, userId, "CONSULTATION_FINAL_DOC");
    }

    /**
     * Get semua file desain untuk konsultasi
     */
    public List<FileUpload> getDesignFiles(UUID consultationId, Long userId) {
        return getConsultationFiles(consultationId, userId, "CONSULTATION_DESIGN_FILE");
    }

    /**
     * Validasi akses konsultasi
     */
    private void validateConsultationAccess(UUID consultationId, Long userId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        // Check if user is part of this consultation
        if (!consultation.getHomeOwner().getId().equals(userId) &&
            !consultation.getConsultant().getId().equals(userId)) {
            throw new DomainException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * Delete file konsultasi
     */
    @Transactional
    public void deleteConsultationFile(UUID consultationId, Long userId, UUID fileId) {
        validateConsultationAccess(consultationId, userId);

        Optional<FileUpload> fileOpt = fileUploadRepository.findByFileId(fileId);
        if (fileOpt.isPresent()) {
            FileUpload file = fileOpt.get();
            // Check if file belongs to consultation usecase
            if (file.getUsecase().startsWith("CONSULTATION_")) {
                fileUploadRepository.delete(file);
            } else {
                throw new DomainException(ErrorCode.UNAUTHORIZED);
            }
        }
    }
}