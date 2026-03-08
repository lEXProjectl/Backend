package com.example.online.dto.auth.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeUserRoleRequest {

    @NotBlank(message = "Новая роль обязательна")
    private String role;
}


