package com.ruan.fincore.service;

import com.ruan.fincore.dto.dashboard.BalanceEvolutionResponse;
import com.ruan.fincore.dto.dashboard.DashboardSummaryResponse;
import com.ruan.fincore.dto.dashboard.ExpensesByCategoryResponse;
import com.ruan.fincore.dto.dashboard.MonthlyEvolutionResponse;
import com.ruan.fincore.dto.dashboard.SavingsRateResponse;
import com.ruan.fincore.enums.TransactionType;
import com.ruan.fincore.repository.AccountRepository;
import com.ruan.fincore.repository.CategorySumProjection;
import com.ruan.fincore.repository.MonthlySumProjection;
import com.ruan.fincore.repository.TransactionRepository;
import com.ruan.fincore.repository.TypeSumProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(String email, Integer month, Integer year) {
        UUID userId = findUserId(email);
        List<TypeSumProjection> sums = transactionRepository.sumByTypeAndPeriod(userId, month, year);
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (TypeSumProjection s : sums) {
            if (s.getType() == TransactionType.INCOME) {
                income = s.getTotal();
            } else {
                expense = s.getTotal();
            }
        }
        return new DashboardSummaryResponse(income, expense, income.subtract(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpensesByCategoryResponse> expensesByCategory(String email, Integer month, Integer year) {
        UUID userId = findUserId(email);
        List<CategorySumProjection> projections = transactionRepository.sumExpensesByCategoryAndPeriod(userId, month, year);
        BigDecimal totalExpenses = projections.stream()
                .map(CategorySumProjection::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return projections.stream()
                .map(p -> {
                    BigDecimal percentage = totalExpenses.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : p.getTotal().divide(totalExpenses, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    return new ExpensesByCategoryResponse(p.getCategoryId(), p.getCategoryName(), p.getTotal(), percentage);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MonthlyEvolutionResponse> monthlyEvolution(String email, int months) {
        UUID userId = findUserId(email);
        LocalDate dateFrom = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
        List<MonthlySumProjection> projections = transactionRepository.sumByTypeAndMonth(userId, dateFrom);
        Map<String, BigDecimal[]> monthlyMap = new HashMap<>();
        for (MonthlySumProjection p : projections) {
            String key = p.getYear() + "-" + p.getMonth();
            monthlyMap.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if ("INCOME".equals(p.getType())) {
                monthlyMap.get(key)[0] = p.getTotal();
            } else {
                monthlyMap.get(key)[1] = p.getTotal();
            }
        }
        List<MonthlyEvolutionResponse> result = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            LocalDate date = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String key = date.getYear() + "-" + date.getMonthValue();
            BigDecimal[] values = monthlyMap.getOrDefault(key, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            result.add(new MonthlyEvolutionResponse(date.getMonthValue(), date.getYear(), values[0], values[1]));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public SavingsRateResponse savingsRate(String email, Integer month, Integer year) {
        UUID userId = findUserId(email);
        DashboardSummaryResponse summary = summary(email, month, year);
        BigDecimal rate = summary.income().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : summary.balance().divide(summary.income(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        return new SavingsRateResponse(month, year, summary.income(), summary.expense(), rate);
    }

    @Transactional(readOnly = true)
    public List<BalanceEvolutionResponse> balanceEvolution(String email, int months) {
        UUID userId = findUserId(email);
        BigDecimal initialBalance = accountRepository.sumBalanceByUserId(userId);

        List<MonthlyEvolutionResponse> evolution = monthlyEvolution(email, months);
        List<BalanceEvolutionResponse> result = new ArrayList<>();
        BigDecimal cumulativeBalance = initialBalance;

        for (int i = evolution.size() - 1; i >= 0; i--) {
            MonthlyEvolutionResponse m = evolution.get(i);
            cumulativeBalance = cumulativeBalance.add(m.income()).subtract(m.expense());
            result.add(new BalanceEvolutionResponse(m.month(), m.year(), cumulativeBalance));
        }

        result.sort((a, b) -> {
            if (!a.year().equals(b.year())) return a.year().compareTo(b.year());
            return a.month().compareTo(b.month());
        });

        return result;
    }

    private UUID findUserId(String email) {
        return userService.findByEmail(email).getId();
    }
}
