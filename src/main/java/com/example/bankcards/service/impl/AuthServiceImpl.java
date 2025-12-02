package com.example.bankcards.service.impl;

import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.dto.jwt.JwtAuthDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * Реализация сервиса аутентификации пользователей.
 * <p>
 * Отвечает за проверку пользовательских учётных данных, генерацию JWT-токенов
 * и обновление access token с помощью refresh token.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Выполняет авторизацию пользователя по логину и паролю.
     * <p>
     * Метод:
     * <ol>
     *     <li>Проверяет существование пользователя по username;</li>
     *     <li>Сравнивает предоставленный пароль с хешированным паролем в базе данных;</li>
     *     <li>В случае успеха генерирует пару токенов — access token и refresh token.</li>
     * </ol>
     *
     * @param dto DTO с логином и паролем
     * @return объект {@link JwtAuthDto} с парой JWT-токенов
     * @throws NotFoundException если пользователь с указанным логином не найден
     * @throws AuthException     если предоставлен неверный пароль
     */
    @Transactional(readOnly = true)
    @Override
    public JwtAuthDto logIn(UserCredentialsDto dto) {
        User user = userRepository.findByUsername(dto.username()).orElseThrow(() -> {
            log.warn("Пользователь с username={} не найден", dto.username());
            return new NotFoundException("Пользователь с username=" + dto.username() + " не найден.");
        });
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            log.warn("Неверный пароль для пользователя={}", user.getUsername());
            throw new AuthException("Неверный пароль.");
        }
        return jwtService.generateAuthToken(user);
    }

    /**
     * Обновляет JWT access token с использованием refresh token.
     * <p>
     * Логика обновления:
     * <ol>
     *     <li>Проверка, что refresh token не равен null;</li>
     *     <li>Валидация refresh token методом {@link JwtService#validateJwtToken(String)};</li>
     *     <li>Извлечение username из токена;</li>
     *     <li>Поиск пользователя в базе;</li>
     *     <li>Генерация нового access token.</li>
     * </ol>
     *
     * @param refreshToken действительный refresh token
     * @return новый объект {@link JwtAuthDto} с обновлёнными токенами
     * @throws AuthException     если refresh token недействителен или отсутствует
     * @throws NotFoundException если пользователь, которому принадлежит токен, не найден
     */
    @Transactional(readOnly = true)
    @Override
    public JwtAuthDto refreshToken(String  refreshToken) {
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            User user = userRepository.findByUsername(jwtService.getUsernameFromToken(refreshToken))
                    .orElseThrow(() -> new NotFoundException("User not found"));
            return jwtService.refreshBaseToken(user, refreshToken);
        }
        throw new AuthException("Invalid refresh token");
    }
}
