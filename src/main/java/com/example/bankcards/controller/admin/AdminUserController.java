package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.page.PageResponse;
import com.example.bankcards.dto.user.UserCreateDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.exception.ErrorResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для административных операций с пользователями.
 * <p>
 * Предоставляет возможности создания новых пользователей и получения списка существующих.
 * Все эндпоинты доступны только администраторам и требуют JWT-аутентификации.
 */
@Tag(name = "admin: Пользователи")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {
    private final UserService userService;

    /**
     * Создаёт нового пользователя от имени администратора.
     * <p>
     * Используется для ручного добавления пользователей в систему.
     * В случае конфликта username выбрасывается ошибка {@code 409 CONFLICT}.
     *
     * @param dto данные для создания нового пользователя
     * @return DTO созданного пользователя
     */
    @Operation(summary = "Создание пользователя админом",
            description = "Создает пользователя и сохраняет его в базу данных")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "(CREATED) Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Неверный формат данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный JWT токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "(CONFLICT) Пользователь с таким username уже существует",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserCreateDto dto) {
        return userService.create(dto);
    }

    /**
     * Возвращает страницу пользователей.
     * <p>
     * Используется администраторами для просмотра всех пользователей системы.
     *
     * @param page номер страницы (начиная с 0)
     * @param size количество элементов на странице
     * @return страницу с DTO пользователей
     */
    @Operation(summary = "Просмотр всех пользователей", description = "Просмотр всех пользователей с пагинацией")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Пользователи получены"),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Неверный формат данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный JWT токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public PageResponse<UserDto> getUsers(@PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
                                          @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        return userService.getUsers(page, size);
    }
}