package com.example.bankcards.controller.user;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardTransferDto;
import com.example.bankcards.dto.page.PageResponse;
import com.example.bankcards.exception.ErrorResponse;
import com.example.bankcards.security.CustomUserDetails;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST-контроллер для работы пользователя со своими банковскими картами.
 * <p>
 * Предоставляет операции просмотра карт, запроса на блокировку и перевода средств
 * между собственными картами. Все эндпоинты требуют аутентификации по JWT.
 */
@Tag(name = "users: Карты")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users/cards")
@RequiredArgsConstructor
@Validated
public class UserCardController {
    private final CardService cardService;
    private final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Возвращает страницу карт текущего пользователя с фильтрацией.
     * <p>
     * Поддерживает фильтрацию по статусу, диапазону дат окончания срока действия
     * и последним четырём цифрам номера карты.
     *
     * @param page            номер страницы (начиная с 0)
     * @param size            размер страницы (количество элементов на странице)
     * @param status          необязательный фильтр по статусу карты
     * @param expiryDateFrom  необязательная нижняя граница срока действия карты (включительно)
     * @param expiryDateTo    необязательная верхняя граница срока действия карты (включительно)
     * @param last4           необязательный фильтр по последним четырём цифрам номера карты
     * @return страничный ответ с DTO карт текущего пользователя
     */
    @Operation(summary = "Получение всех карт пользователя",
            description = "Возвращает все карты пользователя с параметрами пагинации и фильтрации")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Карты возвращены"),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Неверный формат данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный JWT токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public PageResponse<CardDto> getAllUserCards(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
                                                 @Positive @RequestParam(value = "size", defaultValue = "10") int size,
                                                 @RequestParam(value = "status", required = false) CardStatus status,
                                                 @RequestParam(value = "expiryDateFrom", required = false)
                                             @Schema(example = "2025-01-01 12:00:00")
                                             @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime expiryDateFrom,
                                                 @RequestParam(value = "expiryDateTo", required = false)
                                             @Schema(example = "2025-01-01 12:00:00")
                                             @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime expiryDateTo,
                                                 @RequestParam(value = "last4", required = false) String last4) {
        return cardService.getAllUserCards(userDetails.getUsername(), page, size,
                status, expiryDateFrom, expiryDateTo, last4);
    }

    /**
     * Создаёт запрос на блокировку карты от имени текущего пользователя.
     * <p>
     * Доступно только для карт, принадлежащих пользователю и находящихся
     * в статусе {@link CardStatus#ACTIVE}.
     * В случае успеха статус карты изменяется на {@code BLOCK_PENDING}.
     *
     * @param cardId      идентификатор карты, для которой запрашивается блокировка
     * @return DTO карты с обновлённым статусом
     */
    @Operation(summary = "Запрос на блокировку карты", description = "Меняет статус карты на BLOCK_PENDING")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Запрос на блокировку создан"),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный JWT токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "(NOT FOUND) Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "(CONFLICT) Карта неактивна",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{cardId}/block")
    public CardDto blockCardRequest(@PathVariable("cardId") UUID cardId,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        return cardService.blockCardRequest(cardId, userDetails.getUsername());
    }

    /**
     * Выполняет перевод средств между картами текущего пользователя.
     * <p>
     * Перевод возможен только между разными картами, принадлежащими пользователю,
     * при условии, что обе карты активны и на карте-источнике достаточно средств.
     *
     * @param dto         параметры перевода (карта-источник, карта-получатель, сумма)
     */
    @Operation(summary = "Перевод средств", description = "Переводит средства между картами пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Перевод выполнен"),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Некорректное тело запроса",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещён",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = """
            (CONFLICT) Ошибка перевода.
            Причины:
            - Перевод возможен только между своими картами
            - Перевод на ту же карту невозможен
            - Перевод возможен только между активными картами
            - Сумма перевода должна быть больше 0
            - Недостаточно средств
            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/transfer")
    public void transferMoney(@Valid @RequestBody CardTransferDto dto,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        cardService.transfer(userDetails.getUsername(), dto);
    }

    /**
     * Возвращает карту текущего пользователя по её идентификатору.
     * <p>
     *
     * @param cardId      идентификатор карты
     * @return DTO карты
     */
    @Operation(summary = "Получить карту по ID", description = "Возвращает карту пользователя по её ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Карта найдена",
                    content = @Content(schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный или отсутствующий JWT токен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещён",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "(NOT FOUND) Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "(CONFLICT) Просмотр чужой карты",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("{cardId}")
    public CardDto getUserCardById(@PathVariable("cardId") UUID cardId,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        return cardService.getUserCardById(cardId, userDetails.getUsername());
    }
}
