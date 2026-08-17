package com.ruan.fincore.dto.account;

import com.ruan.fincore.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String name,

        @NotNull(message = "Tipo é obrigatório")
        AccountType type,

        @PositiveOrZero(message = "Saldo deve ser maior ou igual a zero")
        BigDecimal balance
) {
}
