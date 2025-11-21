package com.example.bankcards.dto.card;

import com.example.bankcards.util.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO для изменения статуса карты")
public record CardNewStatusDto(
        @Schema(description = "Новый статус карты", example = "BLOCK_PENDING")
        @NotNull
        CardStatus status) {
}