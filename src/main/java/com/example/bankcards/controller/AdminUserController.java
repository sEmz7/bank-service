package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCredentialsDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody UserCredentialsDto dto) {
        return userService.create(dto);
    }

}
