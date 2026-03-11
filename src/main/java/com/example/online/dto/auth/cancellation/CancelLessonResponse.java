package com.example.online.dto.auth.cancellation;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelLessonResponse {

    private Long lessonId;
    private String lessonStatus;
    private boolean balanceChanged;
    private String balanceAction;
    private String message;
}

