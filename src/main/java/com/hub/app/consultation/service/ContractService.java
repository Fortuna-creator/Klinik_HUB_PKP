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
import com.hub.app.escrow.model.EscrowAccount;
import com.hub.app.escrow.service.EscrowService;
import com.hub.app.file.service.FileUploadService;
import com.hub.app.payment.model.Payment;
import com.hub.app.payment.model.PaymentStatus;
import com.hub.app.payment.repository.PaymentRepository;
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
    private final EscrowService escrowService;
    private final PaymentRepository paymentRepository;

    @Transactional
    public ContractResponse createContractDraft(UUID consultationId, Long consultantId,
                                                 ContractGenerationRequest request, MultipartFile file) {
        log.info("=== STARTING CONTRACT DRAFT CREATION ===");
        log.info("Consultation ID: {}, Consultant ID: {}", consultationId, consultantId);

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

        // Calculate payment installments (supports single payment if installments is null/empty)
        List<PaymentInstallment> installments = paymentCalculator.calculateInstallments(
            request.getContractValue(), request.getInstallments());
        log.debug("Calculated {} payment installments", installments.size());

        // Set all required fields before any save operations
        contract.setConsultation(consultation);
        contract.setContractValue(request.getContractValue());
        contract.setStatus(ContractStatus.REQUEST_FOR_APPROVAL);
        contract.setApprovalDeadline(LocalDateTime.now().plusDays(7)); // 7 days approval period

        // Handle file upload if provided
        String fileUrl = null;
        com.hub.app.file.dto.FileUploadResponse uploadResponse = null;
        if (file != null && !file.isEmpty()) {
            // Validate file size (max 2MB)
            long maxFileSize = 2 * 1024 * 1024; // 2MB in bytes
            if (file.getSize() > maxFileSize) {
                throw new RuntimeException("File size exceeds maximum limit of 2MB");
            }

            // Use contract ID for file path - save contract first to get the ID
            contract = contractRepository.save(contract);
            log.debug("Contract saved with ID: {} for file upload", contract.getId());

            try {
                uploadResponse = fileUploadService.uploadFile(
                    "contracts", "draft", contract.getId().toString(), file);
                fileUrl = uploadResponse.getFileUrl();
                log.debug("Contract file uploaded: {}", fileUrl);
            } catch (Exception e) {
                log.error("Failed to upload contract file: {}", e.getMessage());
                throw new RuntimeException("Failed to upload contract file: " + e.getMessage());
            }
        }

        contract.setInstallments(installments); // Store installments in contract
        contract.setFileUrl(fileUrl);

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

        log.info("=== CONTRACT DRAFT CREATION SUCCESSFUL ===");
        ContractResponse response = mapToResponse(contract);

        // Publish event for history tracking only after all database operations succeed
        eventPublisher.publishEvent(new ConsultationEvent(
            this, consultation, "CONTRACT_REQUEST_FOR_APPROVAL", "CONTRACT", "REQUEST_FOR_APPROVAL", "CONSULTANT",
            uploadResponse != null ? Arrays.asList(uploadResponse.getFileId()) : Arrays.asList(),  // Use fileId from upload
            Arrays.asList(),
            request.getContractValue(), null, null, contract.getId()
        ));
        log.info("Contract draft creation event published");

        return response;
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

        // Calculate payment installments with custom percentages from frontend (supports single payment if installments is null/empty)
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

        // Publish event only
        eventPublisher.publishEvent(new ConsultationEvent(
            this, contract.getConsultation(), "CONTRACT_REQUEST_FOR_APPROVAL", "CONTRACT", "REQUEST_FOR_APPROVAL", "CONSULTANT",
            Arrays.asList(), Arrays.asList(), contract.getContractValue(), null, null, contract.getId()
        ));

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

        // Update consultation status to MENUNGGU_TANDA_TANGAN_KONTRAK when contract is approved
        contract.getConsultation().setStatus(com.hub.app.consultation.model.ConsultationStatus.MENUNGGU_TANDA_TANGAN_KONTRAK);
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

        // Publish event only
        eventPublisher.publishEvent(new ConsultationEvent(
            this, contract.getConsultation(), "CONTRACT_REJECTED", "CONTRACT", "REJECTED", "HOMEOWNER",
            Arrays.asList(), Arrays.asList(), null, null, null, contract.getId()
        ));

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

        // Check if contract is in approved status or partial signing status
        if (contract.getStatus() != ContractStatus.APPROVED &&
            contract.getStatus() != ContractStatus.HOMEOWNER_SIGNED &&
            contract.getStatus() != ContractStatus.CONSULTANT_SIGNED) {
            throw new RuntimeException("Contract must be approved before signing");
        }

        boolean isHomeOwner = contract.getConsultation().getHomeOwner().getId().equals(userId);
        ContractStatus newStatus;

        // Determine new status based on who is signing and current status
        if (isHomeOwner) {
            if (contract.getStatus() == ContractStatus.APPROVED) {
                // Homeowner signing first
                newStatus = ContractStatus.HOMEOWNER_SIGNED;
            } else if (contract.getStatus() == ContractStatus.CONSULTANT_SIGNED) {
                // Consultant already signed, homeowner signing now - contract fully signed
                newStatus = ContractStatus.SIGNED;
            } else {
                throw new RuntimeException("Invalid contract status for homeowner signing");
            }
        } else {
            // Consultant signing
            if (contract.getStatus() == ContractStatus.APPROVED) {
                // Consultant signing first
                newStatus = ContractStatus.CONSULTANT_SIGNED;
            } else if (contract.getStatus() == ContractStatus.HOMEOWNER_SIGNED) {
                // Homeowner already signed, consultant signing now - contract fully signed
                newStatus = ContractStatus.SIGNED;
            } else {
                throw new RuntimeException("Invalid contract status for consultant signing");
            }
        }

        contract.setStatus(newStatus);

        // Set individual signed timestamps
        if (isHomeOwner) {
            contract.setHomeownerSignedAt(LocalDateTime.now());
        } else {
            contract.setConsultantSignedAt(LocalDateTime.now());
        }

        // Set signed timestamp only when contract is fully signed
        if (newStatus == ContractStatus.SIGNED) {
            contract.setSignedAt(LocalDateTime.now());
        }

        contract = contractRepository.save(contract);

        // If contract is fully signed, mark consultation as ready for payment request
        if (newStatus == ContractStatus.SIGNED) {
            // Update consultation status to MENUNGGU_REQUEST_PAYMENT after contract is signed
            contract.getConsultation().setStatus(com.hub.app.consultation.model.ConsultationStatus.MENUNGGU_REQUEST_PAYMENT);
            consultationRepository.save(contract.getConsultation());
        } else {
            // Contract partially signed, update consultation status to MENUNGGU_TANDA_TANGAN if not already
            if (contract.getConsultation().getStatus() != ConsultationStatus.MENUNGGU_TANDA_TANGAN_KONTRAK) {
                contract.getConsultation().setStatus(ConsultationStatus.MENUNGGU_TANDA_TANGAN_KONTRAK);
                consultationRepository.save(contract.getConsultation());
            }
        }

        // Create history event for signing - publish after all DB operations
        String signerType = isHomeOwner ? "HOMEOWNER" : "CONSULTANT";
        String signerName = isHomeOwner ? "homeowner" : "konsultan";
        String state = newStatus == ContractStatus.SIGNED ? "SIGNED" :
                      (isHomeOwner ? "HOMEOWNER_SIGNED" : "CONSULTANT_SIGNED");

        // Publish event only
        eventPublisher.publishEvent(new ConsultationEvent(
            this, contract.getConsultation(), "CONTRACT_SIGNED", "CONTRACT", state, signerType,
            Arrays.asList(), Arrays.asList(), contract.getContractValue(), null, null, contract.getId()
        ));

        return mapToResponse(contract);
    }

    @Transactional
    public ContractResponse requestPayment(UUID contractId, Long consultantId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        // Check access - only consultant can request payment
        if (!contract.getConsultation().getConsultant().getId().equals(consultantId)) {
            throw new RuntimeException("Access denied");
        }

        // Check if contract is fully signed
        if (contract.getStatus() != ContractStatus.SIGNED) {
            throw new RuntimeException("Contract must be fully signed before requesting payment");
        }

        // Check if consultation is in MENUNGGU_REQUEST_PAYMENT status
        if (contract.getConsultation().getStatus() != ConsultationStatus.MENUNGGU_REQUEST_PAYMENT) {
            throw new RuntimeException("Invalid consultation status for payment request");
        }

        // Create payment installments after payment is requested
        createPaymentInstallments(contract);

        // Update consultation status to MENUNGGU_PEMBAYARAN after payment request
        contract.getConsultation().setStatus(com.hub.app.consultation.model.ConsultationStatus.MENUNGGU_PEMBAYARAN);
        consultationRepository.save(contract.getConsultation());

        // Get the first payment ID to include in the event
        UUID paymentId = paymentRepository.findByConsultationIdOrderByTerminAsc(contract.getConsultation().getId())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.MENUNGGU)
                .findFirst()
                .map(com.hub.app.payment.model.Payment::getId)
                .orElse(null);

        // Use contract value as total payment amount since all payments are created at this point
        BigDecimal totalPaymentAmount = contract.getContractValue();

        // Publish event for payment request with paymentId and totalPaymentAmount
        eventPublisher.publishEvent(new com.hub.app.consultation.event.ConsultationEvent(
            this, contract.getConsultation(), "PAYMENT_REQUESTED", "PAYMENT", "REQUESTED", "CONSULTANT",
            Arrays.asList(), Arrays.asList(), contract.getContractValue(), null, null, contract.getId(), paymentId, totalPaymentAmount
        ));

        return mapToResponse(contract);
    }

    /**
     * Create payment installments after contract is signed
     */
    private void createPaymentInstallments(Contract contract) {
        // Get installments from contract
        List<PaymentInstallment> installments = contract.getInstallments();

        if (installments == null || installments.isEmpty()) {
            log.warn("No payment installments found in contract, creating single payment installment");
            installments = paymentCalculator.calculateInstallments(contract.getContractValue(), null);
        }

        // Create escrow account for this consultation
        if (!escrowService.escrowExists(contract.getConsultation().getId())) {
            EscrowAccount escrow = escrowService.createEscrow(contract.getConsultation(), contract.getContractValue());
            log.info("Created escrow account {} for consultation {}", escrow.getId(), contract.getConsultation().getId());
        }

        // Create payment records for each installment
        for (PaymentInstallment installment : installments) {
            // Create payment record with MENUNGGU status - home owner needs to pay
            com.hub.app.payment.model.Payment payment = new com.hub.app.payment.model.Payment();
            payment.setConsultation(contract.getConsultation());
            payment.setType(com.hub.app.payment.model.PaymentType.KONSULTASI); // Default to konsultasi
            payment.setTermin(installment.getInstallmentNumber());
            payment.setAmount(installment.getAmount());
            payment.setMethod("BANK_TRANSFER"); // Default method
            payment.setDescription("Pembayaran termin " + installment.getInstallmentNumber() + " konsultasi desain");
            payment.setStatus(com.hub.app.payment.model.PaymentStatus.ESCROW_PENDING); // Waiting for home owner payment
            payment.setDueDate(installment.getDueDate().atStartOfDay()); // Convert LocalDate to LocalDateTime
            payment.setReferenceId(contract.getId().toString()); // Reference to contract

            paymentRepository.save(payment);
            log.info("Created payment installment {} for contract {}: amount={}, due={}",
                    installment.getInstallmentNumber(), contract.getId(),
                    installment.getAmount(), installment.getDueDate());
        }

        log.info("Created {} payment installments for contract {}", installments.size(), contract.getId());
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