package com.example.bankcards.service.impl;

import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.mapper.UserMapper;
import com.example.bankcards.util.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).getContent().stream().map(userMapper::toDto).toList();
    }
}
