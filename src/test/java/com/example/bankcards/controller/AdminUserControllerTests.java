package com.example.bankcards.controller;

import com.example.bankcards.dto.jwt.JwtAuthDto;
import com.example.bankcards.dto.page.PageResponse;
import com.example.bankcards.dto.user.UserCreateDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.util.UserRole;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminUserControllerTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String createAdminAndGetToken() {
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setUsername("admin");
        admin.setPassword("$2a$10$ONXw65Z8qLDXeW3SgwzBiO/dMQ0KvwqN3HNqjqgA/ybHIiHZ2qNDW");
        admin.setRole(UserRole.ROLE_ADMIN);
        userRepository.save(admin);

        JwtAuthDto tokens = jwtService.generateAuthToken(admin);
        return tokens.getToken();
    }

    @Test
    @Sql(scripts = "/data/cleanUp.sql")
    @DisplayName("Удачное создание пользователя админом")
    void positiveCreateUserByAdmin() throws Exception {
        String adminToken = createAdminAndGetToken();

        UserCreateDto createDto = new UserCreateDto("newuser", "pass", UserRole.ROLE_USER);

        String body = objectMapper.writeValueAsString(createDto);

        String response = mvc.perform(post("/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto userDto = objectMapper.readValue(response, UserDto.class);

        assertNotNull(userDto);
        assertEquals(createDto.username(), userDto.username());

        List<User> users = userRepository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    @Sql(scripts = "/data/cleanUp.sql")
    @DisplayName("Создание пользователя пользователем с ролью USER")
    void createUser_ShouldReturn403_WhenNotAdmin() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setPassword("$2a$10$ONXw65Z8qLDXeW3SgwzBiO/dMQ0KvwqN3HNqjqgA/ybHIiHZ2qNDW");
        user.setRole(UserRole.ROLE_USER);
        userRepository.save(user);

        JwtAuthDto tokens = jwtService.generateAuthToken(user);
        String userToken = tokens.getToken();

        UserCreateDto createDto = new UserCreateDto("newuser", "pass", UserRole.ROLE_USER);

        String body = objectMapper.writeValueAsString(createDto);

        mvc.perform(post("/admin/users")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = "/data/cleanUp.sql")
    @DisplayName("Просмотр всех пользователей админом")
    void getUsers_ShouldReturnList_ForAdmin() throws Exception {
        String adminToken = createAdminAndGetToken();

        User user = new User();
        user.setId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
        user.setUsername("user1");
        user.setPassword("$2a$10$REPLACE_ME_WITH_REAL_BCRYPT_HASH");
        user.setRole(UserRole.ROLE_USER);
        userRepository.save(user);

        String response = mvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageResponse<UserDto> users = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertNotNull(users);
        assertEquals(2, users.content().size());
    }
}