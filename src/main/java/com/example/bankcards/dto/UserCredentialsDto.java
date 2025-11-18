package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCredentialsDto(
        @NotBlank
        String username,
        @NotBlank
        String password) {

}