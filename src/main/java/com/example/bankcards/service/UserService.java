package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserCreateDto;
import com.example.bankcards.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(UserCreateDto dto);

    List<UserDto> getUsers(int page, int size);
}
