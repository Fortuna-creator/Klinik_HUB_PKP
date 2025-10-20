package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.Contract;
import com.hub.app.consultation.model.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

    Optional<Contract> findByConsultationIdAndStatus(UUID consultationId, ContractStatus status);
}