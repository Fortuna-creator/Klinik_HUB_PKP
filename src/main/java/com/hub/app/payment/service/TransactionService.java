package com.hub.app.payment.service;

import com.hub.app.payment.model.Transaction;
import com.hub.app.payment.model.TransactionType;
import com.hub.app.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Record a transaction for audit trail
     */
    @Transactional
    public Transaction recordTransaction(Long userId, TransactionType type, BigDecimal amount,
                                       String description, String referenceId) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setCurrency("IDR");
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);

        transaction = transactionRepository.save(transaction);
        log.info("Recorded transaction: {} for user {} - {}", type, userId, amount);

        return transaction;
    }

    /**
     * Record transaction with external reference (for future gateway integration)
     */
    @Transactional
    public Transaction recordTransaction(Long userId, TransactionType type, BigDecimal amount,
                                       String description, String referenceId, String externalReference) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setCurrency("IDR");
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setExternalReference(externalReference);

        transaction = transactionRepository.save(transaction);
        log.info("Recorded transaction with external ref: {} for user {} - {}", type, userId, amount);

        return transaction;
    }
}