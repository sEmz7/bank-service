package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardNewStatusDto;
import com.example.bankcards.dto.card.CardTransferDto;
import com.example.bankcards.dto.page.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Реализация сервиса работы с банковскими картами.
 * <p>
 * Инкапсулирует бизнес-логику создания, обновления, блокировки, перевода средств
 * и получения информации о картах пользователей.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    /**
     * Создаёт новую карту для указанного пользователя.
     * <p>
     * Генерирует номер карты, устанавливает срок действия, начальный баланс и статус,
     * после чего сохраняет карту в базе данных.
     *
     * @param userId идентификатор пользователя, для которого создаётся карта
     * @return DTO созданной карты
     * @throws NotFoundException если пользователь с указанным идентификатором не найден
     */
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

    /**
     * Обновляет статус карты.
     *
     * @param cardId идентификатор карты, статус которой нужно изменить
     * @param dto    DTO с новым статусом карты
     * @return DTO карты с обновлённым статусом
     * @throws NotFoundException если карта с указанным идентификатором не найдена
     */
    @Override
    public CardDto updateCardStatus(UUID cardId, CardNewStatusDto dto) {
        Card card = findCardByIdOrThrow(cardId);
        card.setStatus(dto.status());

        cardRepository.save(card);
        log.debug("Изменен статус карты. cardId={}", cardId);
        return cardMapper.toDto(card);
    }

    /**
     * Удаляет карту по её идентификатору.
     * <p>
     *
     * @param cardId идентификатор карты, подлежащей удалению
     * @throws NotFoundException если карта с указанным идентификатором не найдена
     */
    @Override
    public void deleteCard(UUID cardId) {
        findCardByIdOrThrow(cardId);
        cardRepository.deleteById(cardId);
    }

    /**
     * Возвращает карту по её идентификатору.
     *
     * @param cardId идентификатор карты
     * @return DTO найденной карты
     * @throws NotFoundException если карта с указанным идентификатором не найдена
     */
    @Transactional(readOnly = true)
    @Override
    public CardDto getById(UUID cardId) {
        return cardMapper.toDto(findCardByIdOrThrow(cardId));
    }

    /**
     * Возвращает список карт с поддержкой пагинации и фильтрации по статусу.
     * <p>
     * Используется для административного просмотра всех карт в системе.
     *
     * @param page   номер страницы (начиная с 0)
     * @param size   размер страницы (количество элементов на странице)
     * @param status необязательный фильтр по статусу карты; если {@code null}, возвращаются карты всех статусов
     * @return объект {@link PageResponse} с DTO карт и метаданными пагинации
     */
    @Transactional(readOnly = true)
    @Override
    public PageResponse<CardDto> getAll(int page, int size, CardStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("status"));
        Page<CardDto> cardsPage = cardRepository.findAllByFilter(pageable, status).map(cardMapper::toDto);
        return PageResponse.from(cardsPage);
    }

    /**
     * Возвращает страницу карт конкретного пользователя с учетом фильтров.
     * <p>
     * Поддерживает фильтрацию по статусу, диапазону дат окончания срока действия и последним четырём цифрам.
     *
     * @param username        имя пользователя, чьи карты запрашиваются
     * @param page            номер страницы (начиная с 0)
     * @param size            размер страницы
     * @param status          необязательный фильтр по статусу карты
     * @param expiryDateFrom  нижняя граница срока действия карты (включительно); может быть {@code null}
     * @param expiryDateTo    верхняя граница срока действия карты (включительно); может быть {@code null}
     * @param last4           необязательный фильтр по последним четырём цифрам номера карты
     * @return объект {@link PageResponse} с DTO карт и метаданными пагинации
     * @throws NotFoundException если пользователь с указанным именем не найден
     */
    @Transactional(readOnly = true)
    @Override
    public PageResponse<CardDto> getAllUserCards(String username, int page, int size, CardStatus status,
                                                 LocalDateTime expiryDateFrom, LocalDateTime expiryDateTo, String last4) {
        User user = findUserByUsernameOrThrow(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());

        Page<CardDto> userCardsPage = cardRepository.findAllUserCards(pageable, user.getId(), status, expiryDateFrom,
                expiryDateTo, last4).map(cardMapper::toDto);
        return PageResponse.from(userCardsPage);
    }

    /**
     * Создаёт запрос на блокировку карты от имени пользователя.
     * <p>
     * Доступен только владельцу карты и только для карт со статусом {@link CardStatus#ACTIVE}.
     * В случае успешного запроса статус карты изменяется на {@link CardStatus#BLOCK_PENDING}.
     *
     * @param cardId   идентификатор карты, для которой запрашивается блокировка
     * @param username имя пользователя, от имени которого отправляется запрос
     * @return DTO карты с обновлённым статусом
     * @throws NotFoundException   если пользователь или карта не найдены
     * @throws ConflictException   если карту пытается заблокировать не владелец
     * или текущий статус карты не {@link CardStatus#ACTIVE}
     */
    @Override
    public CardDto blockCardRequest(UUID cardId, String username) {
        User user = findUserByUsernameOrThrow(username);
        Card card = findCardByIdOrThrow(cardId);

        if(!card.getOwner().getId().equals(user.getId())) {
            log.warn("Блокировать карту может только владелец. cardId={}, userId={}", cardId, user.getId());
            throw new ConflictException("Блокировать карту может только владелец.");
        }

        if(!card.getStatus().equals(CardStatus.ACTIVE)) {
            log.warn("Запрос на блокировку карты со статусом не ACTIVE. userId={}, cardId={}",
                    user.getId(), card.getId());
            throw new ConflictException("Статус карты должен быть ACTIVE");
        }

        card.setStatus(CardStatus.BLOCK_PENDING);
        cardRepository.save(card);
        log.debug("Пользователь запросил блокировку карты. userId={}, cardId={}", user.getId(), cardId);
        return cardMapper.toDto(card);
    }

    /**
     * Выполняет перевод средств между двумя картами одного пользователя.
     * <p>
     * Перевод возможен только между разными картами, принадлежащими одному пользователю,
     * со статусом {@link CardStatus#ACTIVE} и при наличии достаточного баланса на карте-источнике.
     *
     * @param username имя пользователя, от имени которого выполняется перевод
     * @param dto      DTO с параметрами перевода (карта-источник, карта-получатель, сумма)
     * @throws NotFoundException   если пользователь или одна из карт не найдены
     * @throws ConflictException   если карты не принадлежат пользователю, совпадают,
     *                             имеют некорректный статус или недостаточно средств/некорректная сумма
     */
    @Override
    public void transfer(String username, CardTransferDto dto) {
        Card fromCard = findCardByIdOrThrow(dto.fromCardId());
        Card toCard = findCardByIdOrThrow(dto.toCardId());
        User user = findUserByUsernameOrThrow(username);

        if (!fromCard.getOwner().getId().equals(user.getId()) || !toCard.getOwner().getId().equals(user.getId())) {
            log.warn("Перевод возможен только между своими картами. fromCardId={}, toCardId={}, userId={}",
                    fromCard.getId(), toCard.getId(), user.getId());
            throw new ConflictException("Перевод возможен только между своими картами.");
        }

        if (fromCard.getId().equals(toCard.getId())) {
            log.warn("Попытка перевода на ту же карту. cardId={}, userId={}", fromCard.getId(), user.getId());
            throw new ConflictException("Перевод на ту же карту невозможен.");
        }

        if (!fromCard.getStatus().equals(CardStatus.ACTIVE) || !toCard.getStatus().equals(CardStatus.ACTIVE)) {
            log.warn("Перевод возможен только между активными картами. fromCardId={}, toCardId={}, userId={}",
                    fromCard.getId(), toCard.getId(), user.getId());
            throw new ConflictException("Перевод возможен только между активными картами.");
        }

        if (dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Некорректная сумма перевода. amount={}, userId={}", dto.amount(), user.getId());
            throw new ConflictException("Сумма перевода должна быть больше 0.");
        }

        if (fromCard.getBalance().compareTo(dto.amount()) < 0) {
            log.warn("Недостаточно средств для перевода. fromCardId={}, balance={}, amount={}, userId={}",
                    fromCard.getId(), fromCard.getBalance(), dto.amount(), user.getId());
            throw new ConflictException("Недостаточно средств для перевода.");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(dto.amount()));
        toCard.setBalance(toCard.getBalance().add(dto.amount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.debug("Успешный перевод между картами. fromCardId={}, toCardId={}, amount={}, userId={}",
                fromCard.getId(), toCard.getId(), dto.amount(), user.getId());
    }

    /**
     * Возвращает карту пользователя по идентификатору, проверяя права доступа.
     * <p>
     * Карта может быть просмотрена только её владельцем.
     *
     * @param cardId   идентификатор карты
     * @param username имя пользователя, запрашивающего карту
     * @return DTO карты
     * @throws NotFoundException   если пользователь или карта не найдены
     * @throws ConflictException   если пользователь пытается просмотреть чужую карту
     */
    @Transactional(readOnly = true)
    @Override
    public CardDto getUserCardById(UUID cardId, String username) {
        User user = findUserByUsernameOrThrow(username);
        Card card = findCardByIdOrThrow(cardId);

        if (!card.getOwner().getId().equals(user.getId())) {
            log.warn("Попытка просмотра чужой карты. cardId={}, userId={}", cardId, user.getId());
            throw new ConflictException("Просмотреть можно только свою карту.");
        }

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
