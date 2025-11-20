package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    @Query("SELECT c FROM Card c " +
            "WHERE (:status IS NULL OR :status = c.status)")
    Page<Card> findAllByFilter(Pageable pageable, CardStatus status);
}
