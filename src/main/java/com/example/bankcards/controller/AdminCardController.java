package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/cards")
@RequiredArgsConstructor
public class AdminCardController {
    private final CardService cardService;

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CardDto createCardForUser(@PathVariable UUID userId) {
        return cardService.createCardForUser(userId);
    }
}
