package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = UserMapper.class)
public interface CardMapper {

    @Mapping(target = "maskedNumber", expression = "java(maskCard(card.getLast4()))")
    CardDto toDto(Card card);

    default String maskCard(String last4) {
        return "**** **** **** " + last4;
    }
}


