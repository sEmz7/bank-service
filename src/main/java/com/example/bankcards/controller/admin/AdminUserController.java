package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserCredentialsDto dto) {
        return userService.create(dto);
    }

    @GetMapping
    public List<UserDto> getUsers(@PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
                                  @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        return userService.getUsers(page, size);
    }
}