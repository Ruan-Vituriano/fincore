package com.ruan.fincore.controller;

import com.ruan.fincore.dto.goal.FinancialGoalProgressResponse;
import com.ruan.fincore.dto.goal.FinancialGoalRequest;
import com.ruan.fincore.dto.goal.FinancialGoalResponse;
import com.ruan.fincore.service.FinancialGoalService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class FinancialGoalController {

    private final FinancialGoalService financialGoalService;

    @GetMapping
    public List<FinancialGoalResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return financialGoalService.list(jwt.getSubject());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialGoalResponse create(@AuthenticationPrincipal Jwt jwt,
                                        @Valid @RequestBody FinancialGoalRequest request) {
        return financialGoalService.create(jwt.getSubject(), request);
    }

    @PutMapping("/{id}")
    public FinancialGoalResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                        @Valid @RequestBody FinancialGoalRequest request) {
        return financialGoalService.update(jwt.getSubject(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        financialGoalService.delete(jwt.getSubject(), id);
    }

    @GetMapping("/{id}/progress")
    public FinancialGoalProgressResponse progress(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return financialGoalService.progress(jwt.getSubject(), id);
    }
}
