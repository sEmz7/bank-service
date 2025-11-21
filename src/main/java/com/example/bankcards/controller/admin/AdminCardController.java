package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardNewStatusDto;
import com.example.bankcards.exception.ErrorResponse;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardStatus;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "admin: Карты")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/cards")
@RequiredArgsConstructor
@Validated
public class AdminCardController {
    private final CardService cardService;

    @Operation(summary = "Создание карты пользователю", description = "Создает карту и сохраняет в базу данных")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "(CREATED) Карта создана"),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Неверный формат данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный JWT токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "(NOT FOUND) Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CardDto createCardForUser(@PathVariable("userId") UUID userId) {
        return cardService.createCardForUser(userId);
    }

    @Operation(summary = "Обновление статуса карты", description = "Обновляет статус карты и сохраняет в базу данных")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Статус карты обновлен"),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Неверный формат данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный JWT токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "(NOT FOUND) Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{cardId}")
    public CardDto updateCardStatus(@PathVariable("cardId") UUID cardId, @Valid @RequestBody CardNewStatusDto dto) {
        return cardService.updateCardStatus(cardId, dto);
    }

    @Operation(summary = "Удаление карты", description = "Удаляет карту")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "(NO CONTENT) Карта удалена"),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный JWT токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "(NOT FOUND) Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable("cardId") UUID cardId) {
        cardService.deleteCard(cardId);
    }

    @Operation(summary = "Получение карты по ID", description = "Возвращает карту по заданному ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Карта получена"),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный JWT токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "(NOT FOUND) Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{cardId}")
    public CardDto getById(@PathVariable("cardId") UUID cardId) {
        return cardService.getById(cardId);
    }

    @Operation(summary = "Получение всех карт",
            description = "Возвращает все карты с параметрами пагинации и фильтром по статусу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Карты получены"),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Неверный формат данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный JWT токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public List<CardDto> getAllCards(@PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
                                     @Positive @RequestParam(value = "size", defaultValue = "10") int size,
                                     @RequestParam(value = "status", required = false) CardStatus status) {
        return cardService.getAll(page, size, status);
    }
}