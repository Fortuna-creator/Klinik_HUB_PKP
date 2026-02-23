package com.hub.app.payment.service;

import com.hub.app.payment.model.Transaction;
import com.hub.app.payment.model.TransactionType;
import com.hub.app.payment.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction();
        testTransaction.setId(java.util.UUID.randomUUID());
        testTransaction.setUserId(1L);
        testTransaction.setType(TransactionType.TOP_UP);
        testTransaction.setAmount(BigDecimal.valueOf(1000));
        testTransaction.setCurrency("IDR");
        testTransaction.setDescription("Test transaction");
        testTransaction.setReferenceId("REF123");
    }

    @Test
    void recordTransaction_ShouldCreateTransaction() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.recordTransaction(
            1L, TransactionType.TOP_UP, BigDecimal.valueOf(1000), "Test transaction", "REF123"
        );

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(TransactionType.TOP_UP, result.getType());
        assertEquals(BigDecimal.valueOf(1000), result.getAmount());
        assertEquals("IDR", result.getCurrency());
        assertEquals("Test transaction", result.getDescription());
        assertEquals("REF123", result.getReferenceId());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction captured = captor.getValue();
        assertEquals(1L, captured.getUserId());
        assertEquals(TransactionType.TOP_UP, captured.getType());
        assertEquals(BigDecimal.valueOf(1000), captured.getAmount());
        assertEquals("IDR", captured.getCurrency());
        assertEquals("Test transaction", captured.getDescription());
        assertEquals("REF123", captured.getReferenceId());
    }

    @Test
    void recordTransactionWithExternalReference_ShouldCreateTransactionWithExternalRef() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.recordTransaction(
            1L, TransactionType.PAYMENT, BigDecimal.valueOf(500), "Payment transaction", "REF456", "EXT789"
        );

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(TransactionType.PAYMENT, result.getType());
        assertEquals(BigDecimal.valueOf(500), result.getAmount());
        assertEquals("IDR", result.getCurrency());
        assertEquals("Payment transaction", result.getDescription());
        assertEquals("REF456", result.getReferenceId());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction captured = captor.getValue();
        assertEquals(1L, captured.getUserId());
        assertEquals(TransactionType.PAYMENT, captured.getType());
        assertEquals(BigDecimal.valueOf(500), captured.getAmount());
        assertEquals("IDR", captured.getCurrency());
        assertEquals("Payment transaction", captured.getDescription());
        assertEquals("REF456", captured.getReferenceId());
        assertEquals("EXT789", captured.getExternalReference());
    }

    @Test
    void recordTransaction_ShouldHandleDifferentTransactionTypes() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Test TOP_UP
        transactionService.recordTransaction(1L, TransactionType.TOP_UP, BigDecimal.valueOf(1000), "Topup", "REF1");
        verify(transactionRepository).save(any(Transaction.class));

        // Test PAYMENT
        transactionService.recordTransaction(1L, TransactionType.PAYMENT, BigDecimal.valueOf(500), "Payment", "REF2");
        verify(transactionRepository, times(2)).save(any(Transaction.class));

        // Test TRANSFER
        transactionService.recordTransaction(1L, TransactionType.TRANSFER, BigDecimal.valueOf(200), "Transfer", "REF3");
        verify(transactionRepository, times(3)).save(any(Transaction.class));
    }

    @Test
    void recordTransaction_ShouldHandleDifferentAmounts() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Test zero amount
        transactionService.recordTransaction(1L, TransactionType.TOP_UP, BigDecimal.ZERO, "Zero amount", "REF1");
        verify(transactionRepository).save(any(Transaction.class));

        // Test large amount
        transactionService.recordTransaction(1L, TransactionType.TOP_UP, BigDecimal.valueOf(1000000), "Large amount", "REF2");
        verify(transactionRepository, times(2)).save(any(Transaction.class));

        // Test decimal amount
        transactionService.recordTransaction(1L, TransactionType.TOP_UP, BigDecimal.valueOf(123.45), "Decimal amount", "REF3");
        verify(transactionRepository, times(3)).save(any(Transaction.class));
    }

    @Test
    void recordTransaction_ShouldHandleNullReferenceId() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.recordTransaction(
            1L, TransactionType.TOP_UP, BigDecimal.valueOf(1000), "Test transaction", null
        );

        assertNotNull(result);
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction captured = captor.getValue();
        assertNull(captured.getReferenceId());
    }

    @Test
    void recordTransaction_ShouldHandleEmptyDescription() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.recordTransaction(
            1L, TransactionType.TOP_UP, BigDecimal.valueOf(1000), "", "REF123"
        );

        assertNotNull(result);
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction captured = captor.getValue();
        assertEquals("", captured.getDescription());
    }
}