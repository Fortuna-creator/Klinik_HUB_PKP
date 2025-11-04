package com.hub.app.wallet.repository;

import com.hub.app.wallet.model.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {

    Optional<UserWallet> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT w FROM UserWallet w WHERE w.user.id = :userId AND w.isActive = true")
    Optional<UserWallet> findActiveByUserId(@Param("userId") Long userId);
}