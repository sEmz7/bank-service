package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserCreateDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserServiceImpl;
import com.example.bankcards.util.UserRole;
import com.example.bankcards.util.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserCreateDto userCreateDto;
    private User user;
    private User savedUser;
    private UserDto userDto;
    private String username;

    @BeforeEach
    void setUp() {
        username = "testuser";

        userCreateDto = new UserCreateDto(
                username,
                "password",
                UserRole.ROLE_USER
        );

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setRole(UserRole.ROLE_USER);

        savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setUsername(username);
        savedUser.setPassword("encoded-password");
        savedUser.setRole(UserRole.ROLE_USER);

        userDto = new UserDto(
                savedUser.getId(),
                savedUser.getUsername()
        );
    }

    @Test
    @DisplayName("create: успешно создаёт пользователя при уникальном username")
    void create_ShouldSaveUser_WhenUsernameNotExists() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userMapper.toEntity(userCreateDto, passwordEncoder)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(userDto);

        UserDto result = userService.create(userCreateDto);

        assertNotNull(result);
        assertEquals(userDto, result);

        verify(userRepository, times(1)).findByUsername(username);
        verify(userMapper, times(1)).toEntity(userCreateDto, passwordEncoder);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toDto(savedUser);
    }

    @Test
    @DisplayName("create: бросает ConflictException, если пользователь с таким username уже существует")
    void create_ShouldThrowConflict_WhenUsernameAlreadyExists() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> userService.create(userCreateDto));

        verify(userRepository, times(1)).findByUsername(username);
        verify(userMapper, never()).toEntity(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUsers: возвращает список пользователей с пагинацией")
    void getUsers_ShouldReturnPagedUsers() {
        int page = 0;
        int size = 2;
        Pageable expectedPageable = PageRequest.of(page, size);

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername("anotherUser");

        UserDto userDto2 = new UserDto(
                user2.getId(),
                user2.getUsername()
        );

        List<User> users = List.of(user, user2);

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(users));
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(userMapper.toDto(user2)).thenReturn(userDto2);

        List<UserDto> result = userService.getUsers(page, size).content();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(userDto, result.get(0));
        assertEquals(userDto2, result.get(1));

        verify(userRepository, times(1)).findAll(expectedPageable);
        verify(userMapper, times(1)).toDto(user);
        verify(userMapper, times(1)).toDto(user2);
    }

    @Test
    @DisplayName("getUsers: возвращает пустой список, если пользователей нет")
    void getUsers_ShouldReturnEmptyList_WhenNoUsers() {
        int page = 0;
        int size = 10;

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<UserDto> result = userService.getUsers(page, size).content();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findAll(PageRequest.of(page, size));
        verifyNoInteractions(userMapper);
    }
}
