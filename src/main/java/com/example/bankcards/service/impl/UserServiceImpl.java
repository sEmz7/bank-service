package com.example.bankcards.service.impl;

import com.example.bankcards.dto.UserCredentialsDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.UserMapper;
import com.example.bankcards.util.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserCredentialsDto dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            log.warn("Пользователь с username = {} уже существует.", dto.username());
            throw new ConflictException("Пользователь с username = " + dto.username() + " уже существует.");
        }
        User user = userMapper.toEntity(dto, passwordEncoder);
        user.setRole(UserRole.ROLE_USER);
        User savedUser = userRepository.save(user);
        log.debug("Пользователь с id={} сохранен.", savedUser.getId());

        return new UserDto(savedUser.getId(), savedUser.getUsername());
    }
}
