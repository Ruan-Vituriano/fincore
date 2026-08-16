package com.ruan.fincore.dto.category;

import com.ruan.fincore.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String name,

        @NotNull(message = "Tipo é obrigatório")
        CategoryType type,

        @Size(max = 60, message = "Ícone deve ter no máximo 60 caracteres")
        String icon,

        @Pattern(regexp = "^(#[0-9A-Fa-f]{6})?$", message = "Cor deve estar no formato hexadecimal #RRGGBB")
        String color
) {
}
