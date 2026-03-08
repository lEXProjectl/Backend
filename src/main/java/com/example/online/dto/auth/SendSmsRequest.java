package com.example.online.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendSmsRequest {

    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(regexp = "^(\\+?7|8)?[0-9]{10}$", message = "Неверный формат номера телефона. Используйте формат: +79991234567 или 89991234567")
    private String phone;
}

