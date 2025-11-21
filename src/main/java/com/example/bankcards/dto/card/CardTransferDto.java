package com.example.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO для перевода средств между картами")
public record CardTransferDto(
        @Schema(description = "ID карты, с которой списываются средства",
                example = "2f16952d-f9bb-458f-ac45-7d8c67109da0")
        @NotNull
        UUID fromCardId,
        @Schema(description = "ID карты, на которую зачисляются средства",
                example = "73bb569b-7e73-47ad-815b-3e5a40c1a6f1")
        @NotNull
        UUID toCardId,
        @Schema(description = "Сумма перевода (должна быть > 0)",
                example = "250.00")
        @NotNull
        BigDecimal amount) {
}
