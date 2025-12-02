package com.example.bankcards.service.impl;

import com.example.bankcards.dto.page.PageResponse;
import com.example.bankcards.dto.user.UserCreateDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация сервиса управления пользователями.
 * <p>
 * Отвечает за создание пользователя и получение всех пользователей
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Создаёт нового пользователя на основе переданного DTO.
     * <p>
     * Метод выполняет:
     * <ol>
     *     <li>Проверку уникальности username;</li>
     *     <li>Хеширование пароля через {@link PasswordEncoder};</li>
     *     <li>Преобразование DTO → entity через {@link UserMapper#toEntity(UserCreateDto, PasswordEncoder)};</li>
     *     <li>Сохранение пользователя в базе данных.</li>
     * </ol>
     *
     * @param dto DTO с данными нового пользователя
     * @return DTO созданного пользователя
     * @throws ConflictException если пользователь с таким username уже существует
     */
    @Override
    public UserDto create(UserCreateDto dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            log.warn("Пользователь с username = {} уже существует.", dto.username());
            throw new ConflictException("Пользователь с username = " + dto.username() + " уже существует.");
        }
        User user = userMapper.toEntity(dto, passwordEncoder);
        User savedUser = userRepository.save(user);
        log.debug("Пользователь с id={} сохранен.", savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    /**
     * Возвращает страницу с пользователями.
     * <p>
     * Используется для административного просмотра всех пользователей.
     *
     * @param page номер страницы (начиная с 0)
     * @param size количество элементов на странице
     * @return объект {@link PageResponse} с метаданными и списком пользователей
     */
    @Transactional(readOnly = true)
    @Override
    public PageResponse<UserDto> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDto> userDtoPage = userRepository.findAll(pageable).map(userMapper::toDto);
        return PageResponse.from(userDtoPage);
    }
}
