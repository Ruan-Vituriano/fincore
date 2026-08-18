package com.ruan.fincore.controller;

import com.ruan.fincore.dto.recurring.GenerateResponse;
import com.ruan.fincore.dto.recurring.RecurringExpenseRequest;
import com.ruan.fincore.dto.recurring.RecurringExpenseResponse;
import com.ruan.fincore.service.RecurringExpenseService;
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
@RequestMapping("/api/v1/recurring-expenses")
@RequiredArgsConstructor
public class RecurringExpenseController {

    private final RecurringExpenseService recurringExpenseService;

    @GetMapping
    public List<RecurringExpenseResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return recurringExpenseService.list(jwt.getSubject());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringExpenseResponse create(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody RecurringExpenseRequest request) {
        return recurringExpenseService.create(jwt.getSubject(), request);
    }

    @PutMapping("/{id}")
    public RecurringExpenseResponse update(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable UUID id,
                                           @Valid @RequestBody RecurringExpenseRequest request) {
        return recurringExpenseService.update(jwt.getSubject(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        recurringExpenseService.delete(jwt.getSubject(), id);
    }

    @PostMapping("/generate")
    public GenerateResponse generate(@AuthenticationPrincipal Jwt jwt) {
        return recurringExpenseService.generateMonthly(jwt.getSubject());
    }
}
