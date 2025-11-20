package com.example.bankcards.dto.card;

import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.util.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDto {
    private UUID id;
    private UserDto owner;
    private String maskedNumber;
    private LocalDateTime expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}
