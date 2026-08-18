package com.ruan.fincore.dto.recurring;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record RecurringExpenseRequest(
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String description,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @NotNull(message = "Categoria é obrigatória")
        UUID categoryId,

        @NotNull(message = "Conta é obrigatória")
        UUID accountId,

        @NotNull(message = "Dia do mês é obrigatório")
        @Min(value = 1, message = "Dia do mês deve estar entre 1 e 31")
        @Max(value = 31, message = "Dia do mês deve estar entre 1 e 31")
        Integer dayOfMonth,

        Boolean isActive
) {
}
