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

    @Query(value = """
            SELECT t.category_id AS categoryId, COALESCE(SUM(t.amount), 0) AS total
            FROM transactions t
            WHERE t.user_id = :userId
              AND t.type = 'EXPENSE'
              AND EXTRACT(MONTH FROM t.date) = :month
              AND EXTRACT(YEAR FROM t.date) = :year
            GROUP BY t.category_id
            """, nativeQuery = true)
    List<CategoryExpenseProjection> sumExpensesByCategoryAndPeriod(@Param("userId") UUID userId,
                                                                    @Param("month") Integer month,
                                                                    @Param("year") Integer year);
}
