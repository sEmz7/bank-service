package com.example.bankcards.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO для авторизации пользователя")
public record UserCredentialsDto(
        @Schema(description = "Имя пользователя", example = "user", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String username,
        @Schema(description = "Пароль", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String password) {

}