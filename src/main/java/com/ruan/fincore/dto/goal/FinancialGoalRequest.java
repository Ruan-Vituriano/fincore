package com.ruan.fincore.dto.goal;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialGoalRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String name,

        @NotNull(message = "Valor da meta é obrigatório")
        @Positive(message = "Valor da meta deve ser maior que zero")
        BigDecimal targetAmount,

        @PositiveOrZero(message = "Valor atual deve ser maior ou igual a zero")
        BigDecimal currentAmount,

        @FutureOrPresent(message = "Prazo deve ser hoje ou uma data futura")
        LocalDate deadline
) {
}
