package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CardNewStatusDto;
import com.example.bankcards.dto.card.CardTransferDto;
import com.example.bankcards.dto.page.PageResponse;
import com.example.bankcards.util.CardStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис управления банковскими картами.
 * <p>
 * Содержит бизнес-логику создания, обновления, блокировки и получения карт,
 * а также перевода средств между картами пользователя.
 */
public interface CardService {
    /**
     * Создаёт новую карту для указанного пользователя.
     *
     * @param userId идентификатор пользователя, которому создаётся карта
     * @return DTO созданной карты
     */
    CardDto createCardForUser(UUID userId);

    /**
     * Обновляет статус карты.
     *
     * @param cardId идентификатор карты
     * @param dto    объект с новым статусом карты
     * @return DTO карты с обновлённым статусом
     */
    CardDto updateCardStatus(UUID cardId, CardNewStatusDto dto);

    /**
     * Удаляет карту по её идентификатору.
     *
     * @param cardId идентификатор удаляемой карты
     */
    void deleteCard(UUID cardId);

    /**
     * Возвращает карту по её идентификатору.
     *
     * @param cardId идентификатор карты
     * @return DTO найденной карты
     */
    CardDto getById(UUID cardId);

    /**
     * Возвращает страницу карт с поддержкой пагинации и,
     * опционально, фильтрацией по статусу.
     *
     * @param page   номер страницы (начиная с 0)
     * @param size   количество элементов на странице
     * @param status фильтр по статусу карты; может быть {@code null}
     * @return объект {@link PageResponse} с DTO карт и метаданными
     */
    PageResponse<CardDto> getAll(int page, int size, CardStatus status);

    /**
     * Возвращает страницу карт пользователя с расширенной фильтрацией.
     *
     * @param username        имя пользователя, чьи карты запрашиваются
     * @param page            номер страницы (начиная с 0)
     * @param size            количество элементов на странице
     * @param status          фильтр по статусу карты; может быть {@code null}
     * @param expiryDateFrom  нижняя граница срока действия карты (включительно); может быть {@code null}
     * @param expiryDateTo    верхняя граница срока действия карты (включительно); может быть {@code null}
     * @param last4           фильтр по последним четырём цифрам номера карты; может быть {@code null}
     * @return объект {@link PageResponse} с DTO карт пользователя
     */
    PageResponse<CardDto> getAllUserCards(String username, int page, int size, CardStatus status,
                                          LocalDateTime expiryDateFrom, LocalDateTime expiryDateTo, String last4);

    /**
     * Создаёт запрос на блокировку карты от имени пользователя.
     *
     * @param cardId   идентификатор карты
     * @param username имя пользователя, отправляющего запрос
     * @return DTO карты с обновлённым статусом
     */
    CardDto blockCardRequest(UUID cardId, String username);

    /**
     * Выполняет перевод средств между картами одного пользователя.
     *
     * @param username имя пользователя, выполняющего перевод
     * @param dto      параметры перевода (карта-источник, карта-получатель, сумма)
     */
    void transfer(String username, CardTransferDto dto);

    /**
     * Возвращает карту пользователя по её идентификатору.
     *
     * @param cardId   идентификатор карты
     * @param username имя пользователя, запрашивающего карту
     * @return DTO найденной карты
     */
    CardDto getUserCardById(UUID cardId, String username);
}
