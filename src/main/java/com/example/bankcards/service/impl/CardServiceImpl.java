package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardNewStatusDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.CardStatus;
import com.example.bankcards.util.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        User user = findUserByIdOrThrow(userId);

        String cardNumber = CardNumberGenerator.generateCardNumber();
        Card card = new Card();
        card.setOwner(user);
        card.setCardNumber(cardNumber);
        card.setLast4(cardNumber.substring(cardNumber.length() - 4));
        card.setExpiryDate(LocalDateTime.now().plusYears(10));
        card.setStatus(CardStatus.PENDING);
        card.setBalance(BigDecimal.ZERO);

        Card saved = cardRepository.save(card);
        log.debug("Карта создана. cardId={}", card.getId());
        return cardMapper.toDto(saved);
    }

    @Override
    public CardDto updateCardStatus(UUID cardId, CardNewStatusDto dto) {
        Card card = findCardByIdOrThrow(cardId);
        card.setStatus(dto.status());

        cardRepository.save(card);
        log.debug("Изменен статус карты. cardId={}", cardId);
        return cardMapper.toDto(card);
    }

    @Override
    public void deleteCard(UUID cardId) {
        cardRepository.deleteById(cardId);
    }

    @Transactional(readOnly = true)
    @Override
    public CardDto getById(UUID cardId) {
        return cardMapper.toDto(findCardByIdOrThrow(cardId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CardDto> getAll(int page, int size, CardStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("status"));
        List<Card> cards = cardRepository.findAllByFilter(pageable, status).getContent();
        return cards.stream().map(cardMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CardDto> getAllUserCards(String username, int page, int size, CardStatus status,
                                         LocalDateTime expiryDateFrom, LocalDateTime expiryDateTo, String last4) {
        User user = findUserByUsernameOrThrow(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());

        List<Card> userCards = cardRepository.findAllUserCards(pageable, user.getId(), status, expiryDateFrom,
                expiryDateTo, last4).getContent();
        return userCards.stream().map(cardMapper::toDto).toList();
    }

    @Override
    public CardDto blockCardRequest(UUID cardId, String username) {
        User user = findUserByUsernameOrThrow(username);
        Card card = findCardByIdOrThrow(cardId);

        if(!card.getOwner().getId().equals(user.getId())) {
            log.warn("Блокировать карту может только владелец. cardId={}, userId={}", cardId, user.getId());
            throw new ConflictException("Блокировать карту может только владелец.");
        }

        card.setStatus(CardStatus.BLOCK_PENDING);
        cardRepository.save(card);
        log.debug("Пользователь запросил блокировку карты. userId={}, cardId={}", user.getId(), cardId);
        return cardMapper.toDto(card);
    }

    private Card findCardByIdOrThrow(UUID cardId) {
        return cardRepository.findById(cardId).orElseThrow(() -> {
            log.warn("Карта с id={} не найдена.", cardId);
            return new NotFoundException("Карта не найдена.");
        });
    }

    private User findUserByIdOrThrow(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id={} не найден.", userId);
            return new NotFoundException("Пользователь с id=" + userId + " не найден.");
        });
    }

    private User findUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("Пользователь с username={} не найден.", username);
            return new NotFoundException("Пользователь не найден.");
        });
    }
}
