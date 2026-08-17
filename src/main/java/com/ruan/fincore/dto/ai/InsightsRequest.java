package com.ruan.fincore.dto.ai;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InsightsRequest(
        @NotNull(message = "Quantidade de meses é obrigatória")
        @Min(value = 1, message = "Mínimo de 1 mês")
        @Max(value = 24, message = "Máximo de 24 meses")
        Integer months
) {
}
