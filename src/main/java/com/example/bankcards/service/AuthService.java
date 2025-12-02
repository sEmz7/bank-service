package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.dto.jwt.JwtAuthDto;

/**
 * Сервис аутентификации пользователей.
 */
public interface AuthService {

    /**
     * Авторизует пользователя по логину и паролю.
     *
     * @param dto учётные данные пользователя
     * @return пара JWT-токенов (access + refresh)
     */
    JwtAuthDto logIn(UserCredentialsDto dto);

    /**
     * Обновляет JWT access token с помощью refresh token.
     *
     * @param refreshToken действительный refresh token
     * @return новая пара токенов
     */
    JwtAuthDto refreshToken(String refreshToken);
}
