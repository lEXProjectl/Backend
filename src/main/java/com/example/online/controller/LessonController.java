package com.example.online.controller;

import com.example.online.dto.auth.lesson.CreateLessonRequest;
import com.example.online.dto.auth.lesson.LessonResponse;
import com.example.online.dto.auth.lesson.UpdateLessonRequest;
import com.example.online.dto.auth.lesson.UpdateLessonStatusRequest;
import com.example.online.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    public ResponseEntity<LessonResponse> createLesson(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateLessonRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lessonService.createLesson(request, principal.getUsername()));
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<LessonResponse>> getSchedule(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(lessonService.getSchedule(from, to, principal.getUsername()));
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    public ResponseEntity<LessonResponse> updateLesson(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long lessonId,
            @RequestBody UpdateLessonRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(lessonService.updateLesson(lessonId, request, principal.getUsername()));
    }

    @PatchMapping("/{lessonId}/status")
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    public ResponseEntity<LessonResponse> updateStatus(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long lessonId,
            @Valid @RequestBody UpdateLessonStatusRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(lessonService.updateStatus(lessonId, request.getStatus(), principal.getUsername()));
    }
}
