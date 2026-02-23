package com.hub.app.escrow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hub.app.escrow.model.EscrowAccount;
import com.hub.app.escrow.model.EscrowStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EscrowAccountRepository extends JpaRepository<EscrowAccount, UUID> {

    Optional<EscrowAccount> findByConsultationId(UUID consultationId);

    boolean existsByConsultationId(UUID consultationId);

    @Query("SELECT e FROM EscrowAccount e WHERE e.consultation.id = :consultationId AND e.status = :status")
    Optional<EscrowAccount> findByConsultationIdAndStatus(@Param("consultationId") UUID consultationId,
                                                         @Param("status") EscrowStatus status);

    @Query("SELECT e FROM EscrowAccount e WHERE e.homeOwnerId = :userId OR e.consultantId = :userId")
    java.util.List<EscrowAccount> findByUserId(@Param("userId") Long userId);
}