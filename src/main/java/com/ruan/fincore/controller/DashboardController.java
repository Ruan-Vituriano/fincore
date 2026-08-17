package com.ruan.fincore.controller;

import com.ruan.fincore.dto.dashboard.DashboardSummaryResponse;
import com.ruan.fincore.dto.dashboard.ExpensesByCategoryResponse;
import com.ruan.fincore.dto.dashboard.MonthlyEvolutionResponse;
import com.ruan.fincore.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return dashboardService.summary(jwt.getSubject(), month, year);
    }

    @GetMapping("/expenses-by-category")
    public List<ExpensesByCategoryResponse> expensesByCategory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return dashboardService.expensesByCategory(jwt.getSubject(), month, year);
    }

    @GetMapping("/monthly-evolution")
    public List<MonthlyEvolutionResponse> monthlyEvolution(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "12") int months) {
        return dashboardService.monthlyEvolution(jwt.getSubject(), months);
    }
}
