package com.example.bankcards.service;

import com.example.bankcards.dto.jwt.JwtAuthDto;
import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private JwtAuthDto jwtAuthDto;
    private String username;
    private String rawPassword;
    private String encodedPassword;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        username = "testuser";
        rawPassword = "password";
        encodedPassword = "encoded-password";
        refreshToken = "refresh-token";

        user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);

        jwtAuthDto = new JwtAuthDto("token", "token");
    }

    @Test
    @DisplayName("logIn: успешная аутентификация при корректных данных")
    void logIn_ShouldReturnTokens_WhenCredentialsValid() {
        UserCredentialsDto dto = new UserCredentialsDto(username, rawPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtService.generateAuthToken(user)).thenReturn(jwtAuthDto);

        JwtAuthDto result = authService.logIn(dto);

        assertNotNull(result);
        assertEquals(jwtAuthDto, result);
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
        verify(jwtService, times(1)).generateAuthToken(user);
    }

    @Test
    @DisplayName("logIn: бросает NotFoundException, если пользователь не найден")
    void logIn_ShouldThrowNotFound_WhenUserNotFound() {
        UserCredentialsDto dto = new UserCredentialsDto(username, rawPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.logIn(dto));

        verify(userRepository, times(1)).findByUsername(username);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("logIn: бросает AuthException при неверном пароле")
    void logIn_ShouldThrowAuthException_WhenPasswordInvalid() {
        UserCredentialsDto dto = new UserCredentialsDto(username, rawPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.logIn(dto));

        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("refreshToken: успешное обновление токена при валидном refreshToken")
    void refreshToken_ShouldReturnNewTokens_WhenRefreshTokenValid() {
        when(jwtService.validateJwtToken(refreshToken)).thenReturn(true);
        when(jwtService.getUsernameFromToken(refreshToken)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(jwtService.refreshBaseToken(user, refreshToken)).thenReturn(jwtAuthDto);

        JwtAuthDto result = authService.refreshToken(refreshToken);

        assertNotNull(result);
        assertEquals(jwtAuthDto, result);
        verify(jwtService, times(1)).validateJwtToken(refreshToken);
        verify(jwtService, times(1)).getUsernameFromToken(refreshToken);
        verify(userRepository, times(1)).findByUsername(username);
        verify(jwtService, times(1)).refreshBaseToken(user, refreshToken);
    }

    @Test
    @DisplayName("refreshToken: бросает AuthException, если refreshToken == null")
    void refreshToken_ShouldThrowAuthException_WhenTokenNull() {
        assertThrows(AuthException.class, () -> authService.refreshToken(null));

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("refreshToken: бросает AuthException при невалидном refreshToken")
    void refreshToken_ShouldThrowAuthException_WhenTokenInvalid() {
        when(jwtService.validateJwtToken(refreshToken)).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.refreshToken(refreshToken));

        verify(jwtService, times(1)).validateJwtToken(refreshToken);
        verify(jwtService, times(0)).getUsernameFromToken(anyString());
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("refreshToken: бросает NotFoundException, если пользователь по токену не найден")
    void refreshToken_ShouldThrowNotFound_WhenUserNotFound() {
        when(jwtService.validateJwtToken(refreshToken)).thenReturn(true);
        when(jwtService.getUsernameFromToken(refreshToken)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.refreshToken(refreshToken));

        verify(jwtService, times(1)).validateJwtToken(refreshToken);
        verify(jwtService, times(1)).getUsernameFromToken(refreshToken);
        verify(userRepository, times(1)).findByUsername(username);
        verify(jwtService, times(0)).refreshBaseToken(any(), anyString());
    }
}
