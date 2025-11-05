package com.hub.app.wallet.service;

import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import com.hub.app.wallet.model.UserWallet;
import com.hub.app.wallet.repository.UserWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private UserWalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletService walletService;

    private User testUser;
    private UserWallet testWallet;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testWallet = new UserWallet();
        testWallet.setId(1L);
        testWallet.setUser(testUser);
        testWallet.setBalance(BigDecimal.valueOf(1000));
        testWallet.setCurrency("IDR");
        testWallet.setIsActive(true);
    }

    @Test
    void getOrCreateWallet_ShouldReturnExistingWallet() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(testWallet));

        UserWallet result = walletService.getOrCreateWallet(1L);

        assertNotNull(result);
        assertEquals(testWallet.getId(), result.getId());
        assertEquals(testWallet.getBalance(), result.getBalance());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void getOrCreateWallet_ShouldCreateNewWallet() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(walletRepository.save(any(UserWallet.class))).thenReturn(testWallet);

        UserWallet result = walletService.getOrCreateWallet(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals("IDR", result.getCurrency());
        assertTrue(result.getIsActive());
        verify(walletRepository).save(any(UserWallet.class));
    }

    @Test
    void getOrCreateWallet_ShouldThrowExceptionWhenUserNotFound() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> walletService.getOrCreateWallet(1L));
    }

    @Test
    void getBalance_ShouldReturnWalletBalance() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(testWallet));

        BigDecimal balance = walletService.getBalance(1L);

        assertEquals(BigDecimal.valueOf(1000), balance);
    }

    @Test
    void addBalance_ShouldIncreaseWalletBalance() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(UserWallet.class))).thenReturn(testWallet);

        UserWallet result = walletService.addBalance(1L, BigDecimal.valueOf(500));

        assertEquals(BigDecimal.valueOf(1500), result.getBalance());
        verify(walletRepository).save(testWallet);
    }

    @Test
    void addBalance_ShouldThrowExceptionForNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.addBalance(1L, BigDecimal.valueOf(-100)));
    }

    @Test
    void addBalance_ShouldThrowExceptionForZeroAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.addBalance(1L, BigDecimal.ZERO));
    }

    @Test
    void deductBalance_ShouldDecreaseWalletBalance() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(UserWallet.class))).thenReturn(testWallet);

        UserWallet result = walletService.deductBalance(1L, BigDecimal.valueOf(300));

        assertEquals(BigDecimal.valueOf(700), result.getBalance());
        verify(walletRepository).save(testWallet);
    }

    @Test
    void deductBalance_ShouldThrowExceptionForNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.deductBalance(1L, BigDecimal.valueOf(-100)));
    }

    @Test
    void deductBalance_ShouldThrowExceptionForZeroAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.deductBalance(1L, BigDecimal.ZERO));
    }

    @Test
    void deductBalance_ShouldThrowExceptionForInsufficientBalance() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(testWallet));

        assertThrows(RuntimeException.class, () -> walletService.deductBalance(1L, BigDecimal.valueOf(2000)));
    }

    @Test
    void transferBalance_ShouldTransferBetweenUsers() {
        UserWallet fromWallet = new UserWallet();
        fromWallet.setId(2L);
        fromWallet.setUser(testUser);
        fromWallet.setBalance(BigDecimal.valueOf(1000));
        fromWallet.setIsActive(true);

        UserWallet toWallet = new UserWallet();
        toWallet.setId(3L);
        toWallet.setUser(testUser);
        toWallet.setBalance(BigDecimal.valueOf(500));
        toWallet.setIsActive(true);

        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findActiveByUserId(2L)).thenReturn(Optional.of(toWallet));
        when(walletRepository.save(any(UserWallet.class))).thenReturn(fromWallet, toWallet);

        walletService.transferBalance(1L, 2L, BigDecimal.valueOf(300), "Test transfer");

        verify(walletRepository, times(2)).save(any(UserWallet.class));
    }

    @Test
    void transferBalance_ShouldThrowExceptionForNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.transferBalance(1L, 2L, BigDecimal.valueOf(-100), "Test"));
    }

    @Test
    void transferBalance_ShouldThrowExceptionForZeroAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.transferBalance(1L, 2L, BigDecimal.ZERO, "Test"));
    }

    @Test
    void transferBalance_ShouldThrowExceptionForSameUser() {
        assertThrows(IllegalArgumentException.class, () -> walletService.transferBalance(1L, 1L, BigDecimal.valueOf(100), "Test"));
    }

    @Test
    void hasSufficientBalance_ShouldReturnTrueWhenBalanceIsSufficient() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(testWallet));

        boolean result = walletService.hasSufficientBalance(1L, BigDecimal.valueOf(500));

        assertTrue(result);
    }

    @Test
    void hasSufficientBalance_ShouldReturnFalseWhenBalanceIsInsufficient() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(testWallet));

        boolean result = walletService.hasSufficientBalance(1L, BigDecimal.valueOf(2000));

        assertFalse(result);
    }

    @Test
    void hasSufficientBalance_ShouldReturnTrueWhenBalanceEqualsAmount() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(testWallet));

        boolean result = walletService.hasSufficientBalance(1L, BigDecimal.valueOf(1000));

        assertTrue(result);
    }

    @Test
    void getWallet_ShouldReturnWallet() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(testWallet));

        UserWallet result = walletService.getWallet(1L);

        assertEquals(testWallet, result);
    }

    @Test
    void processGatewayTopup_ShouldAddBalance() {
        when(walletRepository.findActiveByUserId(1L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(UserWallet.class))).thenReturn(testWallet);

        walletService.processGatewayTopup(1L, BigDecimal.valueOf(500), "REF123");

        verify(walletRepository).save(testWallet);
        assertEquals(BigDecimal.valueOf(1500), testWallet.getBalance());
    }

    @Test
    void processGatewayPayment_ShouldNotThrowException() {
        // This method is a placeholder, so we just verify it doesn't throw
        assertDoesNotThrow(() -> walletService.processGatewayPayment(1L, BigDecimal.valueOf(500), "REF123"));
    }
}