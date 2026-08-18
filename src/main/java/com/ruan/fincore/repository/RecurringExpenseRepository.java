package com.ruan.fincore.repository;

import com.ruan.fincore.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, UUID> {

    List<RecurringExpense> findByUserIdAndIsActiveTrueOrderByDayOfMonthAscDescriptionAsc(UUID userId);

    List<RecurringExpense> findByUserIdOrderByDayOfMonthAscDescriptionAsc(UUID userId);

    Optional<RecurringExpense> findByIdAndUserId(UUID id, UUID userId);
}
