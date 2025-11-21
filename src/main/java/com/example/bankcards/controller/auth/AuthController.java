package com.example.bankcards.controller.auth;

import com.example.bankcards.dto.jwt.JwtAuthDto;
import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Аутентификация")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


    @Operation(description = "Проверяет данные и возвращает jwt токен и refresh token", summary = "Авторизация")
    @ApiResponses({

    })
    @PostMapping("/login")
    public JwtAuthDto logIn(@Valid @RequestBody UserCredentialsDto dto) {
        return authService.logIn(dto);
    }
}
