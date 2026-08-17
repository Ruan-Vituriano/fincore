package com.ruan.fincore.service;

import com.ruan.fincore.dto.ai.InsightsRequest;
import com.ruan.fincore.dto.ai.InsightsResponse;
import com.ruan.fincore.dto.ai.SuggestionRequest;
import com.ruan.fincore.dto.ai.SuggestionResponse;
import com.ruan.fincore.dto.dashboard.DashboardSummaryResponse;
import com.ruan.fincore.dto.dashboard.ExpensesByCategoryResponse;
import com.ruan.fincore.dto.dashboard.MonthlyEvolutionResponse;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AISuggestionService {

    private final ChatModel chatModel;
    private final DashboardService dashboardService;

    @Transactional(readOnly = true)
    public SuggestionResponse suggestion(String email, SuggestionRequest request) {
        DashboardSummaryResponse summary = dashboardService.summary(email, request.month(), request.year());
        List<ExpensesByCategoryResponse> categories = dashboardService.expensesByCategory(email, request.month(), request.year());

        String categoryBreakdown = categories.stream()
                .map(c -> String.format("- %s: R$ %.2f (%.1f%%)", c.categoryName(), c.total(), c.percentage()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
                Você é um consultor financeiro pessoal. Analise o resumo financeiro do mês %d/%d e forneça sugestões práticas e personalizadas para melhorar a situação financeira do usuário.

                Resumo Financeiro:
                - Receitas: R$ %.2f
                - Despesas: R$ %.2f
                - Saldo: R$ %.2f

                Gastos por Categoria:
                %s

                Forneça:
                1. Uma análise breve da situação atual
                2. 3 ações concretas e práticas para melhorar
                3. Uma observação positiva ou motivacional

                Responda em português brasileiro, de forma direta e amigável. Use no máximo 300 palavras.
                """,
                request.month(), request.year(),
                summary.income(), summary.expense(), summary.balance(),
                categoryBreakdown.isEmpty() ? "Nenhum gasto registrado" : categoryBreakdown);

        String response = chatModel.chat(prompt);
        return new SuggestionResponse(response);
    }

    @Transactional(readOnly = true)
    public InsightsResponse insights(String email, InsightsRequest request) {
        List<MonthlyEvolutionResponse> evolution = dashboardService.monthlyEvolution(email, request.months());

        String history = evolution.stream()
                .map(m -> String.format("- %d/%d: Receitas R$ %.2f, Despesas R$ %.2f",
                        m.month(), m.year(), m.income(), m.expense()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
                Você é um analista financeiro especializado em padrões de comportamento. Analise o histórico financeiro dos últimos %d meses e identifique padrões, tendências e oportunidades.

                Histórico Mensal:
                %s

                Forneça:
                1. Padrões de gastos identificados (sazonalidades, tendências)
                2. Comparação entre meses (crescimento ou redução de despesas)
                3. Recomendações baseadas nos padrões encontrados
                4. Previsão para o próximo mês, se possível

                Responda em português brasileiro, de forma analítica mas acessível. Use no máximo 400 palavras.
                """,
                request.months(), history);

        String response = chatModel.chat(prompt);
        return new InsightsResponse(response);
    }
}
