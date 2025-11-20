package com.example.bankcards.controller.auth;

import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.dto.jwt.JwtAuthDto;
import com.example.bankcards.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public JwtAuthDto logIn(@Valid @RequestBody UserCredentialsDto dto) {
        return authService.logIn(dto);
    }
}
