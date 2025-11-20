package com.example.bankcards.service;

import com.example.bankcards.dto.UserCredentialsDto;
import com.example.bankcards.dto.jwt.JwtAuthDto;

public interface AuthService {

    JwtAuthDto logIn(UserCredentialsDto dto);
}
