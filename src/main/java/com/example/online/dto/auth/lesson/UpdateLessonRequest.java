package com.example.online.dto.auth.lesson;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateLessonRequest {

    private String title;
    private String subject;
    private Long teacherId;
    private Long studentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String type;
}
