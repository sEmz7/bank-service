package com.example.bankcards.dto.jwt;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDto(
        @NotBlank
        String refreshToken) {
}