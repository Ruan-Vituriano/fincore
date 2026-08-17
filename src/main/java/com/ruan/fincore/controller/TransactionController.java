package com.ruan.fincore.controller;

import com.ruan.fincore.dto.transaction.TransactionRequest;
import com.ruan.fincore.dto.transaction.TransactionResponse;
import com.ruan.fincore.enums.TransactionType;
import com.ruan.fincore.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public List<TransactionResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) TransactionType type) {
        return transactionService.list(jwt.getSubject(), dateFrom, dateTo, categoryId, accountId, type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<TransactionResponse> create(@AuthenticationPrincipal Jwt jwt,
                                            @Valid @RequestBody TransactionRequest request) {
        return transactionService.create(jwt.getSubject(), request);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                      @Valid @RequestBody TransactionRequest request,
                                      @RequestParam(defaultValue = "false") boolean applyToAll) {
        return transactionService.update(jwt.getSubject(), id, request, applyToAll);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                       @RequestParam(defaultValue = "false") boolean applyToAll) {
        transactionService.delete(jwt.getSubject(), id, applyToAll);
    }

    @GetMapping("/installments/{parentId}")
    public List<TransactionResponse> listInstallments(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable UUID parentId) {
        return transactionService.listInstallments(jwt.getSubject(), parentId);
    }
}
