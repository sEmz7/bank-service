package com.example.bankcards.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO для просмотра пользователя")
public record UserDto(
        @Schema(description = "ID пользователя", example = "2f16952d-f9bb-458f-ac45-7d8c67109da0")
        UUID id,
        @Schema(description = "Имя пользователя", example = "username")
        String username) {
}