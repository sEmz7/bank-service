package com.example.bankcards.dto.card;

import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.util.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "DTO для просмотра карты")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDto {
    @Schema(description = "ID карты", example = "2f16952d-f9bb-458f-ac45-7d8c67109da0")
    private UUID id;
    @Schema(description = "Владелец карты")
    private UserDto owner;
    @Schema(description = "Замаскированный номер карты (только последние 4 цифры)", example = "**** **** **** 1234")
    private String maskedNumber;
    @Schema(description = "Дата окончания срока действия карты", example = "2035-12-31T23:59:59")
    private LocalDateTime expiryDate;
    @Schema(description = "Статус карты", example = "ACTIVE")
    private CardStatus status;
    @Schema(description = "Доступный баланс карты", example = "1500.75")
    private BigDecimal balance;
}
