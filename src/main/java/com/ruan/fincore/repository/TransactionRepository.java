package com.ruan.fincore.repository;

import com.ruan.fincore.entity.Transaction;
import com.ruan.fincore.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND (:dateFrom IS NULL OR t.date >= :dateFrom) " +
            "AND (:dateTo IS NULL OR t.date <= :dateTo) " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND (:accountId IS NULL OR t.account.id = :accountId) " +
            "AND (:type IS NULL OR t.type = :type) " +
            "ORDER BY t.date DESC, t.createdAt DESC")
    List<Transaction> findByFilters(@Param("userId") UUID userId,
                                    @Param("dateFrom") LocalDate dateFrom,
                                    @Param("dateTo") LocalDate dateTo,
                                    @Param("categoryId") UUID categoryId,
                                    @Param("accountId") UUID accountId,
                                    @Param("type") TransactionType type);

    List<Transaction> findByParentTransactionIdOrderByInstallmentNumberAsc(UUID parentTransactionId);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    List<Transaction> findByParentTransactionIdAndUserIdOrderByInstallmentNumberAsc(UUID parentTransactionId, UUID userId);
}
