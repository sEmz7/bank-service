package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardNewStatusDto;
import com.example.bankcards.dto.card.CardTransferDto;
import com.example.bankcards.util.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CardService {
    CardDto createCardForUser(UUID userId);
    CardDto updateCardStatus(UUID cardId, CardNewStatusDto dto);
    void deleteCard(UUID cardId);
    CardDto getById(UUID cardId);
    List<CardDto> getAll(int page, int size, CardStatus status);

    List<CardDto> getAllUserCards(String username, int page, int size, CardStatus status, LocalDateTime expiryDateFrom,
                                  LocalDateTime expiryDateTo, String last4);
    CardDto blockCardRequest(UUID cardId, String username);
    void transfer(String username, CardTransferDto dto);
}
