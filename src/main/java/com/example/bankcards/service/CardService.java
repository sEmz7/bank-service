package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDto;

import java.util.UUID;

public interface CardService {

    CardDto createCardForUser(UUID userId);
}
