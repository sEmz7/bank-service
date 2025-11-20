package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardNewStatusDto;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/cards")
@RequiredArgsConstructor
public class AdminCardController {
    private final CardService cardService;

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CardDto createCardForUser(@PathVariable("userId") UUID userId) {
        return cardService.createCardForUser(userId);
    }

    @PatchMapping("/{cardId}")
    public CardDto updateCardStatus(@PathVariable("cardId") UUID cardId, @Valid @RequestBody CardNewStatusDto dto) {
        return cardService.updateCardStatus(cardId, dto);
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable("cardId") UUID cardId) {
        cardService.deleteCard(cardId);
    }

    @GetMapping("/{cardId}")
    public CardDto getById(@PathVariable("cardId") UUID cardId) {
        return cardService.getById(cardId);
    }

    @GetMapping
    public List<CardDto> getAllCards(@RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "size", defaultValue = "10") int size,
                                     @RequestParam(value = "status", required = false) CardStatus status) {
        return cardService.getAll(page, size, status);
    }
}