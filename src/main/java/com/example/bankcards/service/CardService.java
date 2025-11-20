package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardNewStatusDto;

import java.util.UUID;

public interface CardService {

    CardDto createCardForUser(UUID userId);

    CardDto updateCardStatus(UUID cardId, CardNewStatusDto dto);

    void deleteCard(UUID cardId);

    CardDto getById(UUID cardId);
}
