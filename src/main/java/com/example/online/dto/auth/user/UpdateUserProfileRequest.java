package com.example.online.dto.auth.user;

import lombok.Data;

@Data
public class UpdateUserProfileRequest {

    private String name;
    private String avatar;
}
