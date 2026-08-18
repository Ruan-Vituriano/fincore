package com.ruan.fincore.controller;

import com.ruan.fincore.dto.investment.InvestmentRequest;
import com.ruan.fincore.dto.investment.InvestmentResponse;
import com.ruan.fincore.dto.investment.PortfolioSummaryResponse;
import com.ruan.fincore.service.InvestmentService;
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
@RequestMapping("/api/v1/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @GetMapping
    public List<InvestmentResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return investmentService.list(jwt.getSubject());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentResponse create(@AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody InvestmentRequest request) {
        return investmentService.create(jwt.getSubject(), request);
    }

    @PutMapping("/{id}")
    public InvestmentResponse update(@AuthenticationPrincipal Jwt jwt,
                                     @PathVariable UUID id,
                                     @Valid @RequestBody InvestmentRequest request) {
        return investmentService.update(jwt.getSubject(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        investmentService.delete(jwt.getSubject(), id);
    }

    @GetMapping("/summary")
    public PortfolioSummaryResponse summary(@AuthenticationPrincipal Jwt jwt) {
        return investmentService.summary(jwt.getSubject());
    }
}
