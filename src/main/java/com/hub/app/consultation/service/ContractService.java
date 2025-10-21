package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.ContractResponse;
import com.hub.app.consultation.model.Contract;
import com.hub.app.consultation.model.ContractStatus;
import com.hub.app.consultation.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;

    public ContractResponse getActiveContract(UUID consultationId) {
        Contract contract = contractRepository.findByConsultationIdAndStatus(consultationId, ContractStatus.AKTIF)
                .orElseThrow(() -> new RuntimeException("Active contract not found"));
        return mapToResponse(contract);
    }

    public ContractResponse signContract(UUID contractId, Long userId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        // Check if user has access to this contract
        if (!contract.getConsultation().getHomeOwner().getId().equals(userId) &&
            !contract.getConsultation().getConsultant().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        contract.setDigitalSignature(true);
        contract = contractRepository.save(contract);
        return mapToResponse(contract);
    }

    private ContractResponse mapToResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .consultationId(contract.getConsultation().getId())
                .pdfUrl(contract.getPdfUrl())
                .digitalSignature(contract.getDigitalSignature())
                .status(contract.getStatus())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}