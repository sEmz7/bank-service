package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CardTransferDto(
        @NotNull
        UUID fromCardId,
        @NotNull
        UUID toCardId,
        @NotNull
        BigDecimal amount) {
}
