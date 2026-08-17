package com.ruan.fincore.service;

import com.ruan.fincore.dto.goal.FinancialGoalProgressResponse;
import com.ruan.fincore.dto.goal.FinancialGoalRequest;
import com.ruan.fincore.dto.goal.FinancialGoalResponse;
import com.ruan.fincore.entity.FinancialGoal;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.mapper.FinancialGoalMapper;
import com.ruan.fincore.repository.FinancialGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialGoalService {

    private final FinancialGoalRepository financialGoalRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<FinancialGoalResponse> list(String email) {
        UUID userId = findUserId(email);
        return financialGoalRepository.findByUserIdOrderByName(userId).stream()
                .map(FinancialGoalMapper::toResponse)
                .toList();
    }

    @Transactional
    public FinancialGoalResponse create(String email, FinancialGoalRequest request) {
        User user = userService.findByEmail(email);
        FinancialGoal goal = new FinancialGoal();
        goal.setName(request.name());
        goal.setTargetAmount(request.targetAmount());
        goal.setCurrentAmount(request.currentAmount() != null ? request.currentAmount() : BigDecimal.ZERO);
        goal.setDeadline(request.deadline());
        goal.setUser(user);
        return FinancialGoalMapper.toResponse(financialGoalRepository.save(goal));
    }

    @Transactional
    public FinancialGoalResponse update(String email, UUID id, FinancialGoalRequest request) {
        FinancialGoal goal = findOwnedGoal(id, findUserId(email));
        goal.setName(request.name());
        goal.setTargetAmount(request.targetAmount());
        goal.setCurrentAmount(request.currentAmount() != null ? request.currentAmount() : BigDecimal.ZERO);
        goal.setDeadline(request.deadline());
        return FinancialGoalMapper.toResponse(goal);
    }

    @Transactional
    public void delete(String email, UUID id) {
        FinancialGoal goal = findOwnedGoal(id, findUserId(email));
        financialGoalRepository.delete(goal);
    }

    @Transactional(readOnly = true)
    public FinancialGoalProgressResponse progress(String email, UUID id) {
        FinancialGoal goal = findOwnedGoal(id, findUserId(email));
        BigDecimal percentage = goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : goal.getCurrentAmount().divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
        return new FinancialGoalProgressResponse(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                percentage,
                goal.getDeadline()
        );
    }

    private UUID findUserId(String email) {
        return userService.findByEmail(email).getId();
    }

    private FinancialGoal findOwnedGoal(UUID id, UUID userId) {
        return financialGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta financeira não encontrada"));
    }
}
