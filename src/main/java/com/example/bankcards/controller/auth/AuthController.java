package com.example.bankcards.controller.auth;

import com.example.bankcards.dto.jwt.JwtAuthDto;
import com.example.bankcards.dto.jwt.RefreshTokenDto;
import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.exception.ErrorResponse;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер, отвечающий за операции аутентификации пользователей.
 * <p>
 * Содержит эндпоинты для входа в систему и обновления JWT-токена.
 * Работает с парой токенов: access token и refresh token.
 */
@Tag(name = "Аутентификация")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Аутентифицирует пользователя по предоставленным учётным данным.
     * <p>
     * В случае успешной проверки логина и пароля возвращает пару токенов:
     * <ul>
     *   <li>JWT access token — используется для авторизации в защищённых эндпоинтах</li>
     *   <li>refresh token — используется для получения нового access token</li>
     * </ul>
     *
     * @param dto DTO с логином и паролем пользователя
     * @return объект {@link JwtAuthDto} с access и refresh токенами
     * @throws com.example.bankcards.exception.NotFoundException если пользователь не найден
     * @throws com.example.bankcards.exception.AuthException если пароль неверный
     */
    @Operation(description = "Авторизует пользователя и возвращает токены", summary = "Авторизация")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Пользователь авторизован"),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Неверный формат данных",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный пароль",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "(NOT FOUND) Пользователь на найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public JwtAuthDto logIn(@Valid @RequestBody UserCredentialsDto dto) {
        return authService.logIn(dto);
    }

    /**
     * Выполняет обновление JWT access token с помощью refresh token.
     * <p>
     * Метод принимает действительный refresh token и, в случае успешной валидации,
     * выдаёт пару (новый access token и текущий refresh token).
     *
     * @param refreshTokenDto DTO, содержащее refresh token
     * @return обновлённый {@link JwtAuthDto} с новыми токенами
     * @throws com.example.bankcards.exception.AuthException если refresh token недействителен
     */
    @Operation(summary = "Обновление jwt токена", description = "Проверяет рефреш токен и выдает новый jwt токен")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Токен выдан"),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Неверный формат данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный рефреш токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public JwtAuthDto refreshToken(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        return authService.refreshToken(refreshTokenDto.refreshToken());
    }
}