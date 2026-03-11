package com.example.online.dto.auth.lesson;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLessonStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;
}
