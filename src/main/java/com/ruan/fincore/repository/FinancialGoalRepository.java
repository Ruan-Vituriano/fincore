package com.ruan.fincore.repository;

import com.ruan.fincore.entity.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, UUID> {

    List<FinancialGoal> findByUserIdOrderByName(UUID userId);

    Optional<FinancialGoal> findByIdAndUserId(UUID id, UUID userId);
}
