package com.example.bankcards.entity;

import com.example.bankcards.util.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity банковской карты.
 * <p>
 * Хранит информацию о владельце карты, номере, сроке действия,
 * статусе и текущем балансе. Используется для операций пользователя
 * и административного управления картами.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "cards")
public class Card {
    /**
     * Уникальный идентификатор карты.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * Владелец карты.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    /**
     * Полный номер карты.
     */
    @EqualsAndHashCode.Include
    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber;

    /**
     * Последние четыре цифры номера карты.
     */
    @Column(name = "last4", nullable = false)
    private String last4;

    /**
     * Дата окончания срока действия карты.
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Текущий статус карты.
     */
    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CardStatus status;

    /**
     * Баланс карты.
     */
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
}
