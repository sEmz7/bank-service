package com.example.bankcards.controller;

import com.example.bankcards.dto.jwt.JwtAuthDto;
import com.example.bankcards.dto.jwt.RefreshTokenDto;
import com.example.bankcards.dto.user.UserCredentialsDto;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql"})
    @DisplayName("Удачная авторизация пользователя")
    void positiveLoginTest() throws Exception {

        UserCredentialsDto credentialsDto = new UserCredentialsDto("testuser", "pass");

        String credentialsJson = objectMapper.writeValueAsString(credentialsDto);

        String result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtAuthDto jwtDto = objectMapper.readValue(result, JwtAuthDto.class);

        assertNotNull(jwtDto);
        assertNotNull(jwtDto.getToken());
        assertNotNull(jwtDto.getRefreshToken());
        assertEquals(credentialsDto.username(), jwtService.getUsernameFromToken(jwtDto.getToken()));
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql"})
    @DisplayName("Неудачная авторизация: неверный пароль")
    void negativeLoginTest_WrongPassword() throws Exception {
        UserCredentialsDto credentialsDto = new UserCredentialsDto("testuser", "bad_password");

        String credentialsJson = objectMapper.writeValueAsString(credentialsDto);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql"})
    @DisplayName("Удачное обновление токена")
    void refreshTokenTest() throws Exception {
        UserCredentialsDto credentialsDto = new UserCredentialsDto("testuser", "pass");
        String credentialsJson = objectMapper.writeValueAsString(credentialsDto);

        String loginResult = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtAuthDto jwtDto = objectMapper.readValue(loginResult, JwtAuthDto.class);
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(jwtDto.getRefreshToken());

        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andExpect(status().isOk());
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql"})
    @DisplayName("Неудачное обновление токена: невалидный refresh token")
    void refreshTokenTest_InvalidRefreshToken() throws Exception {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("invalid-refresh-token");

        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andExpect(status().isUnauthorized());
    }
}