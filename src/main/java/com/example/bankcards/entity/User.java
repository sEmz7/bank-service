package com.example.bankcards.entity;

import com.example.bankcards.util.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity пользователя системы.
 * <p>
 * Содержит данные для аутентификации (username, password)
 * и роль пользователя в системе.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Уникальное имя пользователя (логин).
     */
    @EqualsAndHashCode.Include
    @Column(name ="username", nullable = false, unique = true, updatable = false)
    private String username;

    /**
     * Хэш пароля пользователя.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Роль пользователя.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;
}