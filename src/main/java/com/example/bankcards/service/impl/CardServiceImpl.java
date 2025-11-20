package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardNewStatusDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.CardStatus;
import com.example.bankcards.util.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Override
    public CardDto createCardForUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id={} не найден.", userId);
            return new NotFoundException("Пользователь с id=" + userId + " не найден.");
        });
        String cardNumber = CardNumberGenerator.generateCardNumber();
        Card card = new Card();
        card.setOwner(user);
        card.setCardNumber(cardNumber);
        card.setLast4(cardNumber.substring(cardNumber.length() - 4));
        card.setExpiryDate(LocalDateTime.now().plusYears(10));
        card.setStatus(CardStatus.PENDING);
        card.setBalance(BigDecimal.ZERO);

        Card saved = cardRepository.save(card);
        return cardMapper.toDto(saved);
    }

    @Override
    public CardDto updateCardStatus(UUID cardId, CardNewStatusDto dto) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> {
            log.warn("Карта с id={} не найдена.", cardId);
            return new NotFoundException("Карта не найдена.");
        });
        card.setStatus(dto.status());

        cardRepository.save(card);
        return cardMapper.toDto(card);
    }
}
