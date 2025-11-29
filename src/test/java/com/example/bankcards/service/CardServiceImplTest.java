package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardNewStatusDto;
import com.example.bankcards.dto.card.CardTransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.CardStatus;
import com.example.bankcards.util.mapper.CardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private UUID userId;
    private UUID cardId;
    private UUID anotherCardId;
    private User user;
    private Card card;
    private Card anotherCard;
    private CardDto cardDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();
        anotherCardId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        card = new Card();
        card.setId(cardId);
        card.setOwner(user);
        card.setCardNumber("1111222233334444");
        card.setLast4("4444");
        card.setExpiryDate(LocalDateTime.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(new BigDecimal("1000.00"));

        anotherCard = new Card();
        anotherCard.setId(anotherCardId);
        anotherCard.setOwner(user);
        anotherCard.setCardNumber("5555666677778888");
        anotherCard.setLast4("8888");
        anotherCard.setExpiryDate(LocalDateTime.now().plusYears(5));
        anotherCard.setStatus(CardStatus.ACTIVE);
        anotherCard.setBalance(new BigDecimal("500.00"));

        cardDto = new CardDto(
                cardId,
                null,
                "**** **** **** 4444",
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance()
        );
    }

    @Test
    @DisplayName("createCardForUser: создаёт карту для существующего пользователя")
    void createCardForUser_ShouldCreateCard_WhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardMapper.toDto(any(Card.class))).thenReturn(cardDto);

        CardDto result = cardService.createCardForUser(userId);

        assertNotNull(result);
        assertEquals(cardDto, result);
        verify(userRepository, times(1)).findById(userId);
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(cardMapper, times(1)).toDto(any(Card.class));
    }

    @Test
    @DisplayName("createCardForUser: бросает NotFoundException, если пользователь не найден")
    void createCardForUser_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.createCardForUser(userId));

        verify(userRepository, times(1)).findById(userId);
        verifyNoInteractions(cardRepository);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("updateCardStatus: успешно меняет статус карты")
    void updateCardStatus_ShouldUpdateStatus() {
        CardNewStatusDto dto = new CardNewStatusDto(CardStatus.BLOCKED);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.updateCardStatus(cardId, dto);

        assertNotNull(result);
        assertEquals(cardDto, result);
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).save(card);
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    @DisplayName("updateCardStatus: бросает NotFoundException, если карта не найдена")
    void updateCardStatus_ShouldThrowNotFound_WhenCardNotFound() {
        CardNewStatusDto dto = new CardNewStatusDto(CardStatus.BLOCKED);
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.updateCardStatus(cardId, dto));

        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, never()).save(any());
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("deleteCard: удаляет карту по ID")
    void deleteCard_ShouldDelete() {
        cardService.deleteCard(cardId);

        verify(cardRepository, times(1)).deleteById(cardId);
    }

    @Test
    @DisplayName("getById: возвращает карту по ID")
    void getById_ShouldReturnCard() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.getById(cardId);

        assertNotNull(result);
        assertEquals(cardDto, result);
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    @DisplayName("getById: бросает NotFoundException, если карта не найдена")
    void getById_ShouldThrowNotFound_WhenCardNotFound() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.getById(cardId));

        verify(cardRepository, times(1)).findById(cardId);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("getAll: возвращает список карт с фильтром по статусу и пагинацией")
    void getAll_ShouldReturnCards() {
        int page = 0;
        int size = 10;
        CardStatus status = CardStatus.ACTIVE;
        Pageable expectedPageable = PageRequest.of(page, size, Sort.by("status"));

        Card another = new Card();
        another.setId(UUID.randomUUID());
        another.setStatus(CardStatus.ACTIVE);

        CardDto anotherDto = new CardDto(
                another.getId(),
                null,
                "**** **** **** 0000",
                LocalDateTime.now().plusYears(1),
                CardStatus.ACTIVE,
                BigDecimal.ZERO
        );

        when(cardRepository.findAllByFilter(eq(expectedPageable), eq(status)))
                .thenReturn(new PageImpl<>(List.of(card, another)));
        when(cardMapper.toDto(card)).thenReturn(cardDto);
        when(cardMapper.toDto(another)).thenReturn(anotherDto);

        List<CardDto> result = cardService.getAll(page, size, status);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(cardDto, result.get(0));
        assertEquals(anotherDto, result.get(1));

        verify(cardRepository, times(1)).findAllByFilter(expectedPageable, status);
        verify(cardMapper, times(1)).toDto(card);
        verify(cardMapper, times(1)).toDto(another);
    }

    @Test
    @DisplayName("getAll: возвращает пустой список, если карт нет")
    void getAll_ShouldReturnEmptyList_WhenNoCards() {
        int page = 0;
        int size = 10;
        CardStatus status = CardStatus.ACTIVE;
        Pageable expectedPageable = PageRequest.of(page, size, Sort.by("status"));

        when(cardRepository.findAllByFilter(eq(expectedPageable), eq(status)))
                .thenReturn(new PageImpl<>(List.of()));

        List<CardDto> result = cardService.getAll(page, size, status);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(cardRepository, times(1)).findAllByFilter(expectedPageable, status);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("getAllUserCards: возвращает карты пользователя с фильтрами и пагинацией")
    void getAllUserCards_ShouldReturnUserCards() {
        String username = "testuser";
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        String last4 = "4444";
        CardStatus status = CardStatus.ACTIVE;

        Pageable expectedPageable =
                PageRequest.of(0, 10, Sort.by("expiryDate").ascending());

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardRepository.findAllUserCards(
                eq(expectedPageable),
                eq(userId),
                eq(status),
                eq(from),
                eq(to),
                eq(last4))
        ).thenReturn(new PageImpl<>(List.of(card)));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        List<CardDto> result = cardService.getAllUserCards(
                username,
                0,
                10,
                status,
                from,
                to,
                last4
        ).stream().toList();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cardDto, result.getFirst());

        verify(userRepository, times(1)).findByUsername(username);
        verify(cardRepository, times(1)).findAllUserCards(
                expectedPageable, userId, status, from, to, last4
        );
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    @DisplayName("getAllUserCards: бросает NotFoundException, если пользователь не найден")
    void getAllUserCards_ShouldThrowNotFound_WhenUserNotFound() {
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                cardService.getAllUserCards(username, 0, 10, null, null, null, null)
        );

        verify(userRepository, times(1)).findByUsername(username);
        verifyNoInteractions(cardRepository);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("blockCardRequest: успешный запрос на блокировку активной карты владельца")
    void blockCardRequest_ShouldBlock_WhenOwnerAndActive() {
        String username = "testuser";

        card.setStatus(CardStatus.ACTIVE);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.blockCardRequest(cardId, username);

        assertNotNull(result);
        assertEquals(cardDto, result);
        assertEquals(CardStatus.BLOCK_PENDING, card.getStatus());

        verify(userRepository, times(1)).findByUsername(username);
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).save(card);
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    @DisplayName("blockCardRequest: бросает ConflictException, если карту блокирует не владелец")
    void blockCardRequest_ShouldThrowConflict_WhenNotOwner() {
        String username = "testuser";
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());

        card.setOwner(anotherUser);
        card.setStatus(CardStatus.ACTIVE);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(ConflictException.class, () -> cardService.blockCardRequest(cardId, username));

        verify(cardRepository, never()).save(any());
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("blockCardRequest: бросает ConflictException, если статус карты не ACTIVE")
    void blockCardRequest_ShouldThrowConflict_WhenCardNotActive() {
        String username = "testuser";
        card.setStatus(CardStatus.BLOCKED);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(ConflictException.class, () -> cardService.blockCardRequest(cardId, username));

        verify(cardRepository, never()).save(any());
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("transfer: успешный перевод между своими активными картами при достаточном балансе")
    void transfer_ShouldTransfer_WhenAllValid() {
        String username = "testuser";
        BigDecimal amount = new BigDecimal("200.00");

        card.setBalance(new BigDecimal("1000.00"));
        anotherCard.setBalance(new BigDecimal("500.00"));
        card.setStatus(CardStatus.ACTIVE);
        anotherCard.setStatus(CardStatus.ACTIVE);

        CardTransferDto dto = new CardTransferDto(cardId, anotherCardId, amount);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.findById(anotherCardId)).thenReturn(Optional.of(anotherCard));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        cardService.transfer(username, dto);

        assertEquals(new BigDecimal("800.00"), card.getBalance());
        assertEquals(new BigDecimal("700.00"), anotherCard.getBalance());

        verify(cardRepository, times(1)).save(card);
        verify(cardRepository, times(1)).save(anotherCard);
    }

    @Test
    @DisplayName("transfer: бросает ConflictException, если карты не принадлежат пользователю")
    void transfer_ShouldThrowConflict_WhenCardsNotOwnedByUser() {
        String username = "testuser";
        BigDecimal amount = new BigDecimal("100.00");
        CardTransferDto dto = new CardTransferDto(cardId, anotherCardId, amount);

        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        card.setOwner(anotherUser);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.findById(anotherCardId)).thenReturn(Optional.of(anotherCard));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> cardService.transfer(username, dto));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("transfer: бросает ConflictException при переводе на ту же карту")
    void transfer_ShouldThrowConflict_WhenSameCard() {
        String username = "testuser";
        BigDecimal amount = new BigDecimal("100.00");
        CardTransferDto dto = new CardTransferDto(cardId, cardId, amount);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> cardService.transfer(username, dto));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("transfer: бросает ConflictException, если хотя бы одна карта не ACTIVE")
    void transfer_ShouldThrowConflict_WhenCardNotActive() {
        String username = "testuser";
        BigDecimal amount = new BigDecimal("100.00");
        CardTransferDto dto = new CardTransferDto(cardId, anotherCardId, amount);

        card.setStatus(CardStatus.BLOCKED);
        anotherCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.findById(anotherCardId)).thenReturn(Optional.of(anotherCard));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> cardService.transfer(username, dto));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("transfer: бросает ConflictException при сумме перевода ≤ 0")
    void transfer_ShouldThrowConflict_WhenAmountInvalid() {
        String username = "testuser";
        BigDecimal amount = BigDecimal.ZERO;
        CardTransferDto dto = new CardTransferDto(cardId, anotherCardId, amount);

        card.setStatus(CardStatus.ACTIVE);
        anotherCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.findById(anotherCardId)).thenReturn(Optional.of(anotherCard));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> cardService.transfer(username, dto));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("transfer: бросает ConflictException при недостаточном балансе")
    void transfer_ShouldThrowConflict_WhenInsufficientFunds() {
        String username = "testuser";
        BigDecimal amount = new BigDecimal("2000.00");
        CardTransferDto dto = new CardTransferDto(cardId, anotherCardId, amount);

        card.setStatus(CardStatus.ACTIVE);
        anotherCard.setStatus(CardStatus.ACTIVE);
        card.setBalance(new BigDecimal("1000.00"));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.findById(anotherCardId)).thenReturn(Optional.of(anotherCard));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> cardService.transfer(username, dto));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserCardById: возвращает карту пользователя, если он владелец")
    void getUserCardById_ShouldReturnCard_WhenOwner() {
        String username = "testuser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.getUserCardById(cardId, username);

        assertNotNull(result);
        assertEquals(cardDto, result);
        verify(userRepository, times(1)).findByUsername(username);
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    @DisplayName("getUserCardById: бросает NotFoundException, если пользователь не найден")
    void getUserCardById_ShouldThrowNotFound_WhenUserNotFound() {
        String username = "unknown";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.getUserCardById(cardId, username));

        verify(userRepository, times(1)).findByUsername(username);
        verifyNoInteractions(cardRepository);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("getUserCardById: бросает NotFoundException, если карта не найдена")
    void getUserCardById_ShouldThrowNotFound_WhenCardNotFound() {
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.getUserCardById(cardId, username));

        verify(userRepository, times(1)).findByUsername(username);
        verify(cardRepository, times(1)).findById(cardId);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("getUserCardById: бросает ConflictException, если карта чужая")
    void getUserCardById_ShouldThrowConflict_WhenNotOwner() {
        String username = "testuser";
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        card.setOwner(anotherUser);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(ConflictException.class, () -> cardService.getUserCardById(cardId, username));

        verify(cardMapper, never()).toDto(any());
    }
}