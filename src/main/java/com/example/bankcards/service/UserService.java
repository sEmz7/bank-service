package com.example.bankcards.service;

import com.example.bankcards.dto.page.PageResponse;
import com.example.bankcards.dto.user.UserCreateDto;
import com.example.bankcards.dto.user.UserDto;

/**
 * Сервис для управления пользователями.
 */
public interface UserService {

    /**
     * Создаёт нового пользователя.
     *
     * @param dto данные для создания пользователя
     * @return созданный пользователь
     */
    UserDto create(UserCreateDto dto);

    /**
     * Возвращает страницу пользователей.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @return страничный список пользователей
     */
    PageResponse<UserDto> getUsers(int page, int size);
}
