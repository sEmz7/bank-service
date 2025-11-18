package com.example.bankcards.service;

import com.example.bankcards.dto.UserCredentialsDto;
import com.example.bankcards.dto.UserDto;

public interface UserService {

    UserDto create(UserCredentialsDto dto);
}
