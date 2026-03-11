package com.example.online.dto.auth.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterUserRequest {

    @NotBlank(message = "Phone is required")
    private String phone;

    private String name;
}

