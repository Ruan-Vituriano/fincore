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

    @Query(value = """
            SELECT t.type AS type, COALESCE(SUM(t.amount), 0) AS total
            FROM transactions t
            WHERE t.user_id = :userId
              AND EXTRACT(MONTH FROM t.date) = :month
              AND EXTRACT(YEAR FROM t.date) = :year
            GROUP BY t.type
            """, nativeQuery = true)
    List<TypeSumProjection> sumByTypeAndPeriod(@Param("userId") UUID userId,
                                               @Param("month") Integer month,
                                               @Param("year") Integer year);

    @Query(value = """
            SELECT t.category_id AS categoryId, c.name AS categoryName, COALESCE(SUM(t.amount), 0) AS total
            FROM transactions t
            JOIN categories c ON c.id = t.category_id
            WHERE t.user_id = :userId
              AND t.type = 'EXPENSE'
              AND EXTRACT(MONTH FROM t.date) = :month
              AND EXTRACT(YEAR FROM t.date) = :year
            GROUP BY t.category_id, c.name
            ORDER BY SUM(t.amount) DESC
            """, nativeQuery = true)
    List<CategorySumProjection> sumExpensesByCategoryAndPeriod(@Param("userId") UUID userId,
                                                               @Param("month") Integer month,
                                                               @Param("year") Integer year);

    @Query(value = """
            SELECT EXTRACT(MONTH FROM t.date)::int AS month, EXTRACT(YEAR FROM t.date)::int AS year,
                   t.type AS type, COALESCE(SUM(t.amount), 0) AS total
            FROM transactions t
            WHERE t.user_id = :userId
              AND t.date >= :dateFrom
            GROUP BY EXTRACT(MONTH FROM t.date), EXTRACT(YEAR FROM t.date), t.type
            ORDER BY year DESC, month DESC
            """, nativeQuery = true)
    List<MonthlySumProjection> sumByTypeAndMonth(@Param("userId") UUID userId,
                                                  @Param("dateFrom") java.time.LocalDate dateFrom);

    boolean existsByUserIdAndDescriptionAndDateBetweenAndIsRecurringTrue(
            @Param("userId") UUID userId,
            @Param("description") String description,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);
}
