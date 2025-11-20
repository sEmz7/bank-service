package com.example.bankcards.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthDto {
    private String token;
    private String refreshToken;
}
