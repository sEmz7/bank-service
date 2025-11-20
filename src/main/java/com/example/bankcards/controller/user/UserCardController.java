package com.example.bankcards.controller.user;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/users/cards")
@RequiredArgsConstructor
public class UserCardController {
    private final CardService cardService;
    private final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @GetMapping
    List<CardDto> getAllUserCards(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                  @RequestParam(value = "status", required = false) CardStatus status,
                                  @RequestParam(value = "expiryDateFrom", required = false)
                                  @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime expiryDateFrom,
                                  @RequestParam(value = "expiryDateTo", required = false)
                                  @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime expiryDateTo,
                                  @RequestParam(value = "last4", required = false) String last4) {
        return cardService.getAllUserCards(userDetails.getUsername(), page, size,
                status, expiryDateFrom, expiryDateTo, last4);
    }
}
