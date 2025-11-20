package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(UserCredentialsDto dto);

    List<UserDto> getUsers(int page, int size);
}
