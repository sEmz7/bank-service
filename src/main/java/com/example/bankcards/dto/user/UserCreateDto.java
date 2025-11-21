package com.example.bankcards.dto.user;

import com.example.bankcards.util.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO для создания пользователя")
public record UserCreateDto(
        @Schema(description = "Имя пользователя", example = "user", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        String username,
        @Schema(description = "Пароль", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        String password,
        @Schema(description = "Роль пользователя", example = "ROLE_USER", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UserRole role) {
}
