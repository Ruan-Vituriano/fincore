package com.ruan.fincore.repository;

import com.ruan.fincore.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserIdOrderByYearDescMonthDescNameAsc(UUID userId);

    List<Budget> findByUserIdAndMonthAndYearOrderByNameAsc(UUID userId, Integer month, Integer year);

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndCategoryIdAndMonthAndYear(UUID userId, UUID categoryId, Integer month, Integer year);

    boolean existsByUserIdAndCategoryIdAndMonthAndYearAndIdNot(UUID userId, UUID categoryId, Integer month, Integer year, UUID id);

    @Query("""
            SELECT t.category.id AS categoryId, COALESCE(SUM(t.amount), 0) AS total
            FROM Transaction t
            WHERE t.user.id = :userId
              AND t.type = com.ruan.fincore.enums.TransactionType.EXPENSE
              AND FUNCTION('MONTH', t.date) = :month
              AND FUNCTION('YEAR', t.date) = :year
            GROUP BY t.category.id
            """)
    List<CategoryExpenseProjection> sumExpensesByCategoryAndPeriod(@Param("userId") UUID userId,
                                                                    @Param("month") Integer month,
                                                                    @Param("year") Integer year);
}
