package com.example.online.dto.auth.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {

    private Long id;
    private String title;
    private String subject;
    private Long teacherId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String type;
    private String status;
    private List<Long> studentIds;
}
