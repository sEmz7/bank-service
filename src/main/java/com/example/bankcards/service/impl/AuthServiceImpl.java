package com.example.bankcards.service.impl;

import com.example.bankcards.dto.UserCredentialsDto;
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

@RestController
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

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
}
