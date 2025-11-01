package com.hub.app.escrow.service;

import com.hub.app.consultation.model.Consultation;
import com.hub.app.escrow.model.EscrowAccount;
import com.hub.app.escrow.model.EscrowStatus;
import com.hub.app.escrow.repository.EscrowAccountRepository;
import com.hub.app.payment.model.TransactionType;
import com.hub.app.payment.repository.TransactionRepository;
import com.hub.app.payment.service.TransactionService;
import com.hub.app.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowService {

    private final EscrowAccountRepository escrowRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final TransactionService transactionService;

    /**
     * Create escrow account for consultation
     */
    @Transactional
    public EscrowAccount createEscrow(Consultation consultation, BigDecimal amount) {
        if (escrowRepository.existsByConsultationId(consultation.getId())) {
            throw new RuntimeException("Escrow account already exists for this consultation");
        }

        EscrowAccount escrow = new EscrowAccount();
        escrow.setConsultation(consultation);
        escrow.setHomeOwnerId(consultation.getHomeOwner().getId());
        escrow.setConsultantId(consultation.getConsultant().getId());
        escrow.setTotalAmount(amount);
        escrow.setHeldAmount(BigDecimal.ZERO);
        escrow.setStatus(EscrowStatus.PENDING);
        escrow.setDescription("Escrow for consultation: " + consultation.getId());

        escrow = escrowRepository.save(escrow);
        log.info("Created escrow account {} for consultation {}", escrow.getId(), consultation.getId());

        return escrow;
    }

    /**
     * Hold funds in escrow (transfer from home owner wallet to escrow)
     */
    @Transactional
    public EscrowAccount holdFunds(UUID escrowId, BigDecimal amount) {
        EscrowAccount escrow = escrowRepository.findById(escrowId)
                .orElseThrow(() -> new RuntimeException("Escrow account not found"));

        if (escrow.getStatus() != EscrowStatus.PENDING && escrow.getStatus() != EscrowStatus.ACTIVE) {
            throw new RuntimeException("Invalid escrow status for holding funds");
        }

        // Check if home owner has sufficient balance
        if (!walletService.hasSufficientBalance(escrow.getHomeOwnerId(), amount)) {
            throw new RuntimeException("Insufficient balance in home owner wallet");
        }

        // Deduct from home owner wallet
        walletService.deductBalance(escrow.getHomeOwnerId(), amount);

        // Update escrow
        escrow.setHeldAmount(escrow.getHeldAmount().add(amount));
        escrow.setStatus(EscrowStatus.ACTIVE);
        escrow = escrowRepository.save(escrow);

        // Record transaction
        transactionService.recordTransaction(
            escrow.getHomeOwnerId(),
            TransactionType.ESCROW_HOLD,
            amount.negate(), // Negative for debit
            "Funds held in escrow: " + escrow.getId(),
            escrow.getId().toString()
        );

        log.info("Held {} in escrow {} for home owner {}", amount, escrowId, escrow.getHomeOwnerId());
        return escrow;
    }

    /**
     * Release funds from escrow to consultant
     */
    @Transactional
    public EscrowAccount releaseFunds(UUID escrowId) {
        EscrowAccount escrow = escrowRepository.findById(escrowId)
                .orElseThrow(() -> new RuntimeException("Escrow account not found"));

        if (escrow.getStatus() != EscrowStatus.ACTIVE) {
            throw new RuntimeException("Escrow must be active to release funds");
        }

        if (escrow.getHeldAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("No funds to release");
        }

        BigDecimal releaseAmount = escrow.getHeldAmount();

        // Add to consultant wallet
        walletService.addBalance(escrow.getConsultantId(), releaseAmount);

        // Update escrow
        escrow.setHeldAmount(BigDecimal.ZERO);
        escrow.setStatus(EscrowStatus.RELEASED);
        escrow.setReleasedAt(java.time.Instant.now());
        escrow = escrowRepository.save(escrow);

        // Record transaction for consultant
        transactionService.recordTransaction(
            escrow.getConsultantId(),
            TransactionType.ESCROW_RELEASE,
            releaseAmount,
            "Funds released from escrow: " + escrow.getId(),
            escrow.getId().toString()
        );

        log.info("Released {} from escrow {} to consultant {}", releaseAmount, escrowId, escrow.getConsultantId());
        return escrow;
    }

    /**
     * Refund funds from escrow to home owner
     */
    @Transactional
    public EscrowAccount refundFunds(UUID escrowId) {
        EscrowAccount escrow = escrowRepository.findById(escrowId)
                .orElseThrow(() -> new RuntimeException("Escrow account not found"));

        if (escrow.getStatus() != EscrowStatus.ACTIVE) {
            throw new RuntimeException("Escrow must be active to refund funds");
        }

        if (escrow.getHeldAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("No funds to refund");
        }

        BigDecimal refundAmount = escrow.getHeldAmount();

        // Add back to home owner wallet
        walletService.addBalance(escrow.getHomeOwnerId(), refundAmount);

        // Update escrow
        escrow.setHeldAmount(BigDecimal.ZERO);
        escrow.setStatus(EscrowStatus.REFUNDED);
        escrow = escrowRepository.save(escrow);

        // Record transaction
        transactionService.recordTransaction(
            escrow.getHomeOwnerId(),
            TransactionType.ESCROW_REFUND,
            refundAmount,
            "Funds refunded from escrow: " + escrow.getId(),
            escrow.getId().toString()
        );

        log.info("Refunded {} from escrow {} to home owner {}", refundAmount, escrowId, escrow.getHomeOwnerId());
        return escrow;
    }

    /**
     * Get escrow account by consultation ID
     */
    public EscrowAccount getEscrowByConsultationId(UUID consultationId) {
        return escrowRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> new RuntimeException("Escrow account not found for consultation"));
    }

    /**
     * Check if escrow exists for consultation
     */
    public boolean escrowExists(UUID consultationId) {
        return escrowRepository.existsByConsultationId(consultationId);
    }
}