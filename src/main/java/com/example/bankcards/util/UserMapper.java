package com.example.bankcards.util;

import com.example.bankcards.dto.UserCredentialsDto;
import com.example.bankcards.entity.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "password", expression = "java(passwordEncoder.encode(dto.password()))")
    User toEntity(UserCredentialsDto dto, @Context PasswordEncoder passwordEncoder);
}
