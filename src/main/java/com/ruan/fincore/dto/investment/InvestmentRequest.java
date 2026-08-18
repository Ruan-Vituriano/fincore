package com.ruan.fincore.dto.investment;

import com.ruan.fincore.enums.InvestmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestmentRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String name,

        @Size(max = 20, message = "Ticker deve ter no máximo 20 caracteres")
        String ticker,

        @NotNull(message = "Tipo é obrigatório")
        InvestmentType type,

        @NotNull(message = "Valor investido é obrigatório")
        @Positive(message = "Valor investido deve ser maior que zero")
        BigDecimal amountInvested,

        @NotNull(message = "Valor atual é obrigatório")
        @Positive(message = "Valor atual deve ser maior que zero")
        BigDecimal currentValue,

        @NotNull(message = "Data de compra é obrigatória")
        LocalDate purchaseDate,

        @Size(max = 1000, message = "Notas devem ter no máximo 1000 caracteres")
        String notes
) {
}
