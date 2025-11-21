package com.example.bankcards.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "DTO для ошибок")
@Data
@AllArgsConstructor
public class ErrorResponse {
    @Schema(description = "Описание ошибки")
    private String message;
}
