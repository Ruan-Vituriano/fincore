package com.ruan.fincore.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String description,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @NotNull(message = "Data é obrigatória")
        LocalDate date,

        @NotNull(message = "Categoria é obrigatória")
        UUID categoryId,

        @NotNull(message = "Conta é obrigatória")
        UUID accountId,

        @Size(max = 1000, message = "Notas devem ter no máximo 1000 caracteres")
        String notes,

        Boolean isRecurring,

        Integer totalInstallments
) {
}
