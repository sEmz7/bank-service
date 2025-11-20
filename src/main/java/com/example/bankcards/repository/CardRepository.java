package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    @Query("SELECT c FROM Card c " +
            "WHERE (:status IS NULL OR :status = c.status)")
    Page<Card> findAllByFilter(Pageable pageable, CardStatus status);

    @Query("SELECT c FROM Card c " +
            "WHERE c.owner.id = :userId " +
            "AND (:status IS NULL OR c.status = :status) " +
            "AND (cast(:expiryDateFrom as date) IS NULL OR c.expiryDate >= :expiryDateFrom) " +
            "AND (cast(:expiryDateTo as date) IS NULL OR c.expiryDate <= :expiryDateTo) " +
            "AND (:last4 IS NULL OR c.last4 = :last4)")
    Page<Card> findAllUserCards(Pageable pageable, UUID userId, CardStatus status, LocalDateTime expiryDateFrom,
                                LocalDateTime expiryDateTo, String last4);
}