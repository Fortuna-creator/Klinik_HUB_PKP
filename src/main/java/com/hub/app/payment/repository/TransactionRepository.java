package com.hub.app.payment.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hub.app.payment.model.Transaction;
import com.hub.app.payment.model.TransactionType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Transaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long userId, Instant startDate, Instant endDate);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.type IN :types ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndTypeIn(@Param("userId") Long userId,
                                           @Param("types") List<TransactionType> types);

    @Query("SELECT t FROM Transaction t WHERE t.referenceId = :referenceId ORDER BY t.createdAt DESC")
    List<Transaction> findByReferenceId(@Param("referenceId") String referenceId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.createdAt >= :since")
    java.math.BigDecimal sumAmountByUserIdAndTypeSince(@Param("userId") Long userId,
                                                      @Param("type") TransactionType type,
                                                      @Param("since") Instant since);
}