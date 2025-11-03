package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.ContractGenerationRequest;
import com.hub.app.consultation.dto.ContractResponse;
import com.hub.app.consultation.dto.PaymentInstallment;
import com.hub.app.consultation.event.ConsultationEvent;
import com.hub.app.consultation.model.Contract;
import com.hub.app.consultation.model.ContractStatus;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.repository.ContractRepository;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.file.service.FileUploadService;
import com.hub.app.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final ConsultationRepository consultationRepository;
    private final ProjectRepository projectRepository;
    private final ConsultationHistoryService historyService;
    private final ContractTemplateService contractTemplateService;
    private final PaymentInstallmentCalculator paymentCalculator;
    private final ApplicationEventPublisher eventPublisher;
    private final FileUploadService fileUploadService;

    @Transactional
    public ContractResponse createContractDraft(UUID consultationId, Long consultantId,
                                               ContractGenerationRequest request, MultipartFile file) {
        log.info("=== STARTING CONTRACT DRAFT CREATION ===");
        log.info("Consultation ID: {}, Consultant ID: {}", consultationId, consultantId);

        try {
            Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                    .orElseThrow(() -> new RuntimeException("Consultation not found"));
            log.debug("Consultation found: ID={}, Status={}", consultation.getId(), consultation.getStatus());

            // Check if consultation status is SURVEY_SELESAI or MENYIAPKAN_KONTRAK (revision)
            if (consultation.getStatus() != ConsultationStatus.SURVEY_SELESAI && consultation.getStatus() != ConsultationStatus.MENYIAPKAN_KONTRAK) {
                log.error("Invalid consultation status for contract creation: expected={}, actual={}",
                         ConsultationStatus.SURVEY_SELESAI, consultation.getStatus());
                throw new RuntimeException("Contract can only be created after survey is completed");
            }

            Contract contract = new Contract();

            // Check if contract already exists - allow only if previous contract is rejected or revision requested
            if (contractRepository.findByConsultationId(consultationId).isPresent()) {
                Contract existingContract = contractRepository.findByConsultationId(consultationId).get();
                if (existingContract.getStatus() != ContractStatus.REJECTED &&
                    existingContract.getStatus() != ContractStatus.DRAFT && existingContract.getStatus() != ContractStatus.REVISION_REQUESTED) {
                    log.error("Contract already exists for consultation ID: {} with status: {}", consultationId, existingContract.getStatus());
                    throw new RuntimeException("Contract already exists for this consultation");
                }

                contract = existingContract;
                log.info("Creating new contract draft for consultation ID: {} (previous contract status: {})", consultationId, existingContract.getStatus());
            }

            // Handle file upload if provided
            String fileUrl = null;
            com.hub.app.file.dto.FileUploadResponse uploadResponse = null;
            if (file != null && !file.isEmpty()) {
                // Validate file size (max 2MB)
                long maxFileSize = 2 * 1024 * 1024; // 2MB in bytes
                if (file.getSize() > maxFileSize) {
                    throw new RuntimeException("File size exceeds maximum limit of 2MB");
                }

                try {
                    // Use contract ID for file path - save contract first to get the ID
                    contract = contractRepository.save(contract);
                    log.debug("Contract saved with ID: {} for file upload", contract.getId());

                    uploadResponse = fileUploadService.uploadFile(
                        "contracts", "draft", contract.getId().toString(), file);
                    fileUrl = uploadResponse.getFileUrl();
                    log.debug("Contract file uploaded: {}", fileUrl);
                } catch (Exception e) {
                    log.error("Failed to upload contract file: {}", e.getMessage());
                    throw new RuntimeException("Failed to upload contract file: " + e.getMessage());
                }
            }

            contract.setConsultation(consultation);
            contract.setContractValue(request.getContractValue());
            contract.setFileUrl(fileUrl);
            contract.setStatus(ContractStatus.REQUEST_FOR_APPROVAL);
            contract.setApprovalDeadline(LocalDateTime.now().plusDays(7)); // 7 days approval period

            // Only save if not already saved for file upload
            if (contract.getId() == null) {
                contract = contractRepository.save(contract);
                log.debug("Contract draft created: ID={}", contract.getId());
            } else {
                contract = contractRepository.save(contract);
                log.debug("Contract draft updated: ID={}", contract.getId());
            }

            // Update consultation status to MENUNGGU_APPROVAL_KONTRAK
            consultation.setStatus(ConsultationStatus.MENUNGGU_APPROVAL_KONTRAK);
            consultationRepository.save(consultation);
            log.info("Consultation status updated to MENUNGGU_APPROVAL_KONTRAK");

            // Publish event for history tracking
            eventPublisher.publishEvent(new ConsultationEvent(
                this, consultation, "CONTRACT_REQUEST_FOR_APPROVAL", "CONTRACT", "REQUEST_FOR_APPROVAL", "CONSULTANT",
                uploadResponse != null ? Arrays.asList(uploadResponse.getFileId()) : Arrays.asList(),  // Use fileId from upload
                Arrays.asList(),
                request.getContractValue(), null, null, contract.getId()
            ));
            log.info("Contract draft creation event published");

            log.info("=== CONTRACT DRAFT CREATION SUCCESSFUL ===");
            return mapToResponse(contract);
        } catch (RuntimeException e) {
            log.error("=== CONTRACT DRAFT CREATION FAILED - RUNTIME EXCEPTION ===");
            log.error("Consultation ID: {}, Consultant ID: {}, Error: {}", consultationId, consultantId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== CONTRACT DRAFT CREATION FAILED - UNEXPECTED ERROR ===");
            log.error("Consultation ID: {}, Consultant ID: {}, Error: {}", consultationId, consultantId, e.getMessage(), e);
            throw new RuntimeException("Failed to create contract draft", e);
        }
    }

    public byte[] generateContractDraft(UUID consultationId, Long consultantId, ContractGenerationRequest request) {
        log.info("Generating contract draft document for consultationId: {}, consultantId: {}", consultationId, consultantId);

        // Validate consultation exists and belongs to consultant
        log.debug("Validating consultation exists and belongs to consultant");
        Consultation consultation = consultationRepository.findByIdAndConsultantId(consultationId, consultantId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));
        log.debug("Consultation found: {}", consultation.getId());

        // Check if consultation status is SURVEY_SELESAI
        if (consultation.getStatus() != com.hub.app.consultation.model.ConsultationStatus.SURVEY_SELESAI) {
            log.warn("Invalid consultation status for contract generation: expected={}, actual={}",
                    com.hub.app.consultation.model.ConsultationStatus.SURVEY_SELESAI, consultation.getStatus());
            throw new RuntimeException("Contract can only be created after survey is completed");
        }

        // Get project information for location
        log.debug("Retrieving project information for location details");
        com.hub.app.project.model.Project project = projectRepository.findByProjectId(consultation.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        log.debug("Project found: {}", project.getProjectId());

        // Calculate payment installments with custom percentages from frontend
        log.debug("Calculating payment installments with contract value: {}", request.getContractValue());
        List<PaymentInstallment> installments = paymentCalculator.calculateInstallments(
            request.getContractValue(), request.getInstallments());
        log.debug("Calculated {} installments", installments.size());

        // Generate contract document from template
        byte[] contractDocument;
        try {
            log.info("Generating contract document from template");
            contractDocument = contractTemplateService.generateContractDocument(
                consultation.getHomeOwner().getName(),
                consultation.getConsultant().getName(),
                request.getContractValue(),
                project.getLocationDetail(),
                consultation.getHomeOwner().getAddress(),
                consultation.getConsultant().getAddress(),
                installments
            );
            log.info("Contract document generated successfully, size: {} bytes", contractDocument.length);
        } catch (Exception e) {
            log.error("Failed to generate contract document for consultationId: {}", consultationId, e);
            throw new RuntimeException("Failed to generate contract document", e);
        }

        return contractDocument;
    }

    @Transactional
    public ContractResponse requestContractApproval(UUID contractId, Long consultantId) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getConsultation().getConsultant().getId().equals(consultantId)) {
            throw new RuntimeException("Access denied");
        }

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new RuntimeException("Invalid contract status for approval request");
        }

        contract.setStatus(ContractStatus.REQUEST_FOR_APPROVAL);
        contract = contractRepository.save(contract);

        // Update consultation status
        contract.getConsultation().setStatus(com.hub.app.consultation.model.ConsultationStatus.MENUNGGU_APPROVAL_KONTRAK);
        consultationRepository.save(contract.getConsultation());

        // Create history event
        historyService.createHistoryEvent(
            contract.getConsultation(),
            "CONTRACT",
            "REQUEST_FOR_APPROVAL",
            "Kontrak diajukan untuk approval homeowner",
            "Kontrak konsultasi desain diajukan untuk persetujuan homeowner",
            java.util.Arrays.asList(),  // No fileId available in this method
            java.util.Arrays.asList(),
            "CONSULTANT",
            "REQUEST_FOR_APPROVAL",
            "Nilai kontrak: Rp " + contract.getContractValue().toString(),
            null, null, null, contract.getId()
        );

        return mapToResponse(contract);
    }

    @Transactional
    public ContractResponse approveContract(UUID contractId, Long homeOwnerId) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getConsultation().getHomeOwner().getId().equals(homeOwnerId)) {
            throw new RuntimeException("Access denied");
        }

        if (contract.getStatus() != ContractStatus.REQUEST_FOR_APPROVAL) {
            throw new RuntimeException("Invalid contract status for approval");
        }

        contract.setStatus(ContractStatus.APPROVED);
        contract.setApprovedAt(LocalDateTime.now());
        contract = contractRepository.save(contract);

        // Update consultation status
        contract.getConsultation().setStatus(com.hub.app.consultation.model.ConsultationStatus.KONTRAK_DISETUJUI);
        consultationRepository.save(contract.getConsultation());

        // Publish event for history tracking
        eventPublisher.publishEvent(new ConsultationEvent(
            this, contract.getConsultation(), "CONTRACT_APPROVED", "CONTRACT", "APPROVED", "HOMEOWNER",
            Arrays.asList(),  // No files for approval event
            Arrays.asList(),
            contract.getContractValue(), null, null, contract.getId()
        ));

        return mapToResponse(contract);
    }

    @Transactional
    public ContractResponse rejectContract(UUID contractId, Long homeOwnerId, String reason) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getConsultation().getHomeOwner().getId().equals(homeOwnerId)) {
            throw new RuntimeException("Access denied");
        }

        contract.setStatus(ContractStatus.REJECTED);
        contract.setNotes(reason);
        contract = contractRepository.save(contract);

        // Update consultation status back to SURVEY_SELESAI since contract is rejected
        contract.getConsultation().setStatus(com.hub.app.consultation.model.ConsultationStatus.SURVEY_SELESAI);
        consultationRepository.save(contract.getConsultation());

        // Create history event
        historyService.createHistoryEvent(
            contract.getConsultation(),
            "CONTRACT",
            "REJECTED",
            "Kontrak ditolak homeowner",
            "Homeowner menolak kontrak dengan alasan: " + reason,
            java.util.Arrays.asList(),  // No files for rejection
            java.util.Arrays.asList(),
            "HOMEOWNER",
            "REJECTED",
            reason,
            null, null, null, contract.getId()
        );

        return mapToResponse(contract);
    }

    public ContractResponse getContract(UUID consultationId, Long userId) {

        Contract contract = contractRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        // Check access
        if (!contract.getConsultation().getHomeOwner().getId().equals(userId) &&
            !contract.getConsultation().getConsultant().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(contract);
    }

    @Transactional
    public ContractResponse requestContractRevision(UUID contractId, Long homeOwnerId, String revisionNotes) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getConsultation().getHomeOwner().getId().equals(homeOwnerId)) {
            throw new RuntimeException("Access denied");
        }

        if (contract.getStatus() != ContractStatus.REQUEST_FOR_APPROVAL) {
            throw new RuntimeException("Invalid contract status for revision request");
        }

        // Update existing contract status to revision requested
        contract.setStatus(ContractStatus.REVISION_REQUESTED);
        contract.setNotes("Revision requested: " + revisionNotes);
        contract = contractRepository.save(contract);

        // Update consultation status back to MENYIAPKAN_KONTRAK since contract revision is requested
        contract.getConsultation().setStatus(ConsultationStatus.MENYIAPKAN_KONTRAK);
        consultationRepository.save(contract.getConsultation());

        // Publish event for history tracking
        eventPublisher.publishEvent(new ConsultationEvent(
            this, contract.getConsultation(), "CONTRACT_REVISION_REQUESTED", "CONTRACT", "REVISION_REQUESTED", "HOMEOWNER",
            Arrays.asList(),  // No files for revision request
            Arrays.asList(),
            null, null, null, contract.getId()
        ));

        return mapToResponse(contract);
    }

    @Transactional
    public ContractResponse signContract(UUID contractId, Long userId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        // Check access - both homeowner and consultant can sign
        if (!contract.getConsultation().getHomeOwner().getId().equals(userId) &&
            !contract.getConsultation().getConsultant().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // Check if contract is in approved status
        if (contract.getStatus() != ContractStatus.APPROVED) {
            throw new RuntimeException("Contract must be approved before signing");
        }

        contract.setStatus(ContractStatus.SIGNED);
        contract.setSignedAt(LocalDateTime.now());
        contract = contractRepository.save(contract);

        // Update consultation status to AKTIF after contract is signed
        contract.getConsultation().setStatus(com.hub.app.consultation.model.ConsultationStatus.AKTIF);
        contract.getConsultation().setStartedAt(java.time.Instant.now());
        consultationRepository.save(contract.getConsultation());

        // Publish event
        eventPublisher.publishEvent(new com.hub.app.consultation.event.ConsultationEvent(
            this, contract.getConsultation(), "CONSULTATION_STARTED", "CONSULTATION", "STARTED", "SYSTEM",
            Arrays.asList(), Arrays.asList(), null, null, null, contract.getId()  // No files for consultation start
        ));

        // Create history event
        historyService.createHistoryEvent(
            contract.getConsultation(),
            "CONTRACT",
            "SIGNED",
            "Kontrak ditandatangani",
            "Kontrak konsultasi desain telah ditandatangani oleh " +
            (contract.getConsultation().getHomeOwner().getId().equals(userId) ? "homeowner" : "konsultan"),
            java.util.Arrays.asList(),  // No fileId available, contract.getFileUrl() is URL
            java.util.Arrays.asList(),
            contract.getConsultation().getHomeOwner().getId().equals(userId) ? "HOMEOWNER" : "CONSULTANT",
            "SIGNED",
            "Kontrak telah ditandatangani",
            null, null, null, contract.getId()
        );

        // Create history event for consultation start
        historyService.createHistoryEvent(
            contract.getConsultation(),
            "CONSULTATION",
            "STARTED",
            "Konsultasi aktif dimulai",
            "Konsultasi desain telah dimulai setelah kontrak ditandatangani",
            java.util.Arrays.asList(),  // No files for consultation start
            java.util.Arrays.asList(),
            "SYSTEM",
            "STARTED",
            "Konsultasi aktif"
        );

        return mapToResponse(contract);
    }

    private ContractResponse mapToResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .consultationId(contract.getConsultation().getId())
                .fileUrl(contract.getFileUrl())
                .digitalSignature(contract.getSignedAt() != null)
                .status(contract.getStatus())
                .createdAt(contract.getCreatedAt() != null ? contract.getCreatedAt().toInstant(java.time.ZoneOffset.UTC) : null)
                .updatedAt(contract.getUpdatedAt() != null ? contract.getUpdatedAt().toInstant(java.time.ZoneOffset.UTC) : null)
                .build();
    }
}