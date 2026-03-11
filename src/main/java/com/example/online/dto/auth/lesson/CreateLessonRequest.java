package com.example.online.dto.auth.lesson;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateLessonRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String subject;

    private Long teacherId;

    private Long studentId;

    @NotNull(message = "startTime is required")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    private LocalDateTime endTime;

    private String type;
}

