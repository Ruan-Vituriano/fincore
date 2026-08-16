package com.ruan.fincore.controller;

import com.ruan.fincore.dto.budget.BudgetRequest;
import com.ruan.fincore.dto.budget.BudgetResponse;
import com.ruan.fincore.dto.budget.BudgetSummaryResponse;
import com.ruan.fincore.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public List<BudgetResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return budgetService.list(jwt.getSubject(), month, year);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BudgetRequest request) {
        return budgetService.create(jwt.getSubject(), request);
    }

    @PutMapping("/{id}")
    public BudgetResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                 @Valid @RequestBody BudgetRequest request) {
        return budgetService.update(jwt.getSubject(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        budgetService.delete(jwt.getSubject(), id);
    }

    @GetMapping("/summary")
    public List<BudgetSummaryResponse> summary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return budgetService.summary(jwt.getSubject(), month, year);
    }
}
