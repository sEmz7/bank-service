package com.example.bankcards.entity;

import com.example.bankcards.util.CardStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(name = "card_number")
    private String cardNumber;

    private String last4;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Enumerated(value = EnumType.STRING)
    private CardStatus status;

    private BigDecimal balance;
}
