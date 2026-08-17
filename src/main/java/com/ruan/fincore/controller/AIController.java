package com.ruan.fincore.controller;

import com.ruan.fincore.dto.ai.InsightsRequest;
import com.ruan.fincore.dto.ai.InsightsResponse;
import com.ruan.fincore.dto.ai.SuggestionRequest;
import com.ruan.fincore.dto.ai.SuggestionResponse;
import com.ruan.fincore.service.AISuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AISuggestionService aiSuggestionService;

    @PostMapping("/suggestion")
    public SuggestionResponse suggestion(@AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody SuggestionRequest request) {
        return aiSuggestionService.suggestion(jwt.getSubject(), request);
    }

    @PostMapping("/insights")
    public InsightsResponse insights(@AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody InsightsRequest request) {
        return aiSuggestionService.insights(jwt.getSubject(), request);
    }
}
