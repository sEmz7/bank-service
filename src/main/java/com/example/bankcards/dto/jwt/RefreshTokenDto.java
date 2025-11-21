package com.example.bankcards.dto.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO для refresh токена")
public record RefreshTokenDto(
        @Schema(description = "Refresh токен", example = "token")
        @NotBlank
        String refreshToken) {
}