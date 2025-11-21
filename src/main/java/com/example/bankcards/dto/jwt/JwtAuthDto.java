package com.example.bankcards.dto.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO для jwt и refresh токенов")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthDto {
    @Schema(description = "JWT токен", example = "token")
    private String token;

    @Schema(description = "Refresh токен", example = "token")
    private String refreshToken;
}
