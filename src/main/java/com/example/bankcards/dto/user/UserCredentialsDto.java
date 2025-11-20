package com.example.bankcards.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserCredentialsDto(
        @NotBlank
        String username,
        @NotBlank
        String password) {

}