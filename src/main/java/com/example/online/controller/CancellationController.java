package com.example.online.controller;

import com.example.online.dto.auth.cancellation.CancelLessonResponse;
import com.example.online.service.CancellationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cancellations")
@RequiredArgsConstructor
public class CancellationController {

    private final CancellationService cancellationService;

    @PostMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyAuthority('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<CancelLessonResponse> cancelLesson(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long lessonId
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(cancellationService.cancelLesson(lessonId, principal.getUsername()));
    }
}
