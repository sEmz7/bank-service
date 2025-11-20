package com.example.bankcards.dto.user;

import com.example.bankcards.util.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserCreateDto(
        @NotNull
        String username,
        @NotNull
        String password,
        @NotNull
        UserRole role) {
}
