package com.hub.app.wallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import com.hub.app.wallet.model.UserWallet;
import com.hub.app.wallet.repository.UserWalletRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final UserWalletRepository walletRepository;
    private final UserRepository userRepository;

    /**
     * Get or create wallet for user
     */
    @Transactional
    public UserWallet getOrCreateWallet(Long userId) {
        Optional<UserWallet> existingWallet = walletRepository.findActiveByUserId(userId);

        if (existingWallet.isPresent()) {
            return existingWallet.get();
        }

        // Create new wallet
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserWallet wallet = new UserWallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("IDR");
        wallet.setIsActive(true);

        wallet = walletRepository.save(wallet);
        log.info("Created new wallet for user ID: {}", userId);

        return wallet;
    }

    /**
     * Get wallet balance
     */
    public BigDecimal getBalance(Long userId) {
        UserWallet wallet = getOrCreateWallet(userId);
        return wallet.getBalance();
    }

    /**
     * Add balance to wallet (for simulation/admin purposes)
     */
    @Transactional
    public UserWallet addBalance(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        UserWallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet = walletRepository.save(wallet);

        log.info("Added {} to wallet for user ID: {}", amount, userId);
        return wallet;
    }

    /**
     * Deduct balance from wallet
     */
    @Transactional
    public UserWallet deductBalance(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        UserWallet wallet = getOrCreateWallet(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet = walletRepository.save(wallet);

        log.info("Deducted {} from wallet for user ID: {}", amount, userId);
        return wallet;
    }

    /**
     * Transfer balance between users
     */
    @Transactional
    public void transferBalance(Long fromUserId, Long toUserId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("Cannot transfer to same user");
        }

        // Deduct from sender
        deductBalance(fromUserId, amount);

        // Add to receiver
        addBalance(toUserId, amount);

        log.info("Transferred {} from user {} to user {}: {}", amount, fromUserId, toUserId, description);
    }

    /**
     * Check if user has sufficient balance
     */
    public boolean hasSufficientBalance(Long userId, BigDecimal amount) {
        BigDecimal balance = getBalance(userId);
        return balance.compareTo(amount) >= 0;
    }

    /**
     * Get wallet details
     */
    public UserWallet getWallet(Long userId) {
        return getOrCreateWallet(userId);
    }

    // Placeholder methods for future payment gateway integration
    public void processGatewayTopup(Long userId, BigDecimal amount, String gatewayReference) {
        // TODO: Implement gateway integration
        log.info("Gateway topup placeholder: user={}, amount={}, ref={}", userId, amount, gatewayReference);
        addBalance(userId, amount);
    }

    public void processGatewayPayment(Long userId, BigDecimal amount, String gatewayReference) {
        // TODO: Implement gateway integration
        log.info("Gateway payment placeholder: user={}, amount={}, ref={}", userId, amount, gatewayReference);
        // This would typically deduct from external account and add to wallet
    }
}