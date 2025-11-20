package com.example.bankcards.dto.card;

import com.example.bankcards.util.CardStatus;
import jakarta.validation.constraints.NotNull;

public record CardNewStatusDto(
        @NotNull
        CardStatus status) {
}