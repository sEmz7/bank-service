package com.example.bankcards.controller.user;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardTransferDto;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/cards")
@RequiredArgsConstructor
@Validated
public class UserCardController {
    private final CardService cardService;
    private final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @GetMapping
    public List<CardDto> getAllUserCards(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
                                  @Positive @RequestParam(value = "size", defaultValue = "10") int size,
                                  @RequestParam(value = "status", required = false) CardStatus status,
                                  @RequestParam(value = "expiryDateFrom", required = false)
                                  @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime expiryDateFrom,
                                  @RequestParam(value = "expiryDateTo", required = false)
                                  @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime expiryDateTo,
                                  @RequestParam(value = "last4", required = false) String last4) {
        return cardService.getAllUserCards(userDetails.getUsername(), page, size,
                status, expiryDateFrom, expiryDateTo, last4);
    }

    @PatchMapping("/{cardId}/block")
    public CardDto blockCardRequest(@PathVariable("cardId") UUID cardId,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        return cardService.blockCardRequest(cardId, userDetails.getUsername());
    }

    @PostMapping("/transfer")
    public void transferMoney(@Valid @RequestBody CardTransferDto dto,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        cardService.transfer(userDetails.getUsername(), dto);
    }
}
