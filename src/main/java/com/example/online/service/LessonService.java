package com.example.online.service;

import com.example.online.dto.auth.lesson.CreateLessonRequest;
import com.example.online.dto.auth.lesson.LessonResponse;
import com.example.online.dto.auth.lesson.UpdateLessonRequest;
import com.example.online.entity.Lesson;
import com.example.online.entity.LessonParticipant;
import com.example.online.entity.User;
import com.example.online.entity.enums.LessonStatus;
import com.example.online.entity.enums.LessonType;
import com.example.online.entity.enums.Role;
import com.example.online.repository.LessonParticipantRepository;
import com.example.online.repository.LessonRepository;
import com.example.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonParticipantRepository lessonParticipantRepository;
    private final UserRepository userRepository;

    @Transactional
    public LessonResponse createLesson(CreateLessonRequest request, String actorPhone) {
        User actor = findUserByPhone(actorPhone);
        User teacher = resolveTeacher(actor, request.getTeacherId());

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime should be after startTime");
        }

        Lesson lesson = Lesson.builder()
                .title(request.getTitle())
                .subject(request.getSubject())
                .teacher(teacher)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .type(parseLessonType(request.getType()))
                .status(LessonStatus.PLANNED)
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);

        if (request.getStudentId() != null) {
            User student = findUserById(request.getStudentId());
            lessonParticipantRepository.save(LessonParticipant.builder()
                    .lesson(savedLesson)
                    .student(student)
                    .build());
        }

        return toDto(savedLesson);
    }

    public List<LessonResponse> getSchedule(LocalDateTime from, LocalDateTime to, String actorPhone) {
        User actor = findUserByPhone(actorPhone);

        if (actor.getRole() == Role.ADMIN) {
            return lessonRepository.findByStartTimeBetweenOrderByStartTimeAsc(from, to)
                    .stream()
                    .map(this::toDto)
                    .toList();
        }

        if (actor.getRole() == Role.TEACHER) {
            return lessonRepository.findByTeacherAndStartTimeBetweenOrderByStartTimeAsc(actor, from, to)
                    .stream()
                    .map(this::toDto)
                    .toList();
        }

        return lessonParticipantRepository.findByStudentAndLessonStartTimeBetweenOrderByLessonStartTimeAsc(actor, from, to)
                .stream()
                .map(LessonParticipant::getLesson)
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public LessonResponse updateLesson(Long lessonId, UpdateLessonRequest request, String actorPhone) {
        User actor = findUserByPhone(actorPhone);
        Lesson lesson = findLessonById(lessonId);
        validateLessonAccess(actor, lesson);

        if (request.getTitle() != null) {
            lesson.setTitle(request.getTitle());
        }
        if (request.getSubject() != null) {
            lesson.setSubject(request.getSubject());
        }
        if (request.getStartTime() != null) {
            lesson.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            lesson.setEndTime(request.getEndTime());
        }
        if (lesson.getEndTime() != null && lesson.getStartTime() != null && lesson.getEndTime().isBefore(lesson.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime should be after startTime");
        }
        if (request.getType() != null) {
            lesson.setType(parseLessonType(request.getType()));
        }

        if (request.getTeacherId() != null) {
            lesson.setTeacher(resolveTeacher(actor, request.getTeacherId()));
        }

        Lesson saved = lessonRepository.save(lesson);

        if (request.getStudentId() != null) {
            User student = findUserById(request.getStudentId());
            lessonParticipantRepository.deleteByLesson(saved);
            lessonParticipantRepository.save(LessonParticipant.builder()
                    .lesson(saved)
                    .student(student)
                    .build());
        }

        return toDto(saved);
    }

    @Transactional
    public LessonResponse updateStatus(Long lessonId, String statusRaw, String actorPhone) {
        User actor = findUserByPhone(actorPhone);
        Lesson lesson = findLessonById(lessonId);
        validateLessonAccess(actor, lesson);

        LessonStatus status;
        try {
            status = LessonStatus.valueOf(statusRaw);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Allowed statuses: PLANNED, COMPLETED, TEACHER_ABSENT, STUDENT_ABSENT, CANCELLED");
        }

        lesson.setStatus(status);
        return toDto(lessonRepository.save(lesson));
    }

    private User resolveTeacher(User actor, Long requestedTeacherId) {
        if (actor.getRole() == Role.TEACHER) {
            if (requestedTeacherId != null && !actor.getId().equals(requestedTeacherId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher can create lessons only for themselves");
            }
            return actor;
        }

        if (actor.getRole() == Role.ADMIN) {
            if (requestedTeacherId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teacherId is required for ADMIN");
            }
            User teacher = findUserById(requestedTeacherId);
            if (teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teacherId must belong to TEACHER or ADMIN");
            }
            return teacher;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only TEACHER or ADMIN can manage lessons");
    }

    private void validateLessonAccess(User actor, Lesson lesson) {
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (actor.getRole() == Role.TEACHER && lesson.getTeacher() != null && actor.getId().equals(lesson.getTeacher().getId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this lesson");
    }

    private LessonType parseLessonType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return LessonType.INDIVIDUAL;
        }

        try {
            return LessonType.valueOf(rawType);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed types: GROUP, INDIVIDUAL");
        }
    }

    private User findUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Lesson findLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
    }

    private LessonResponse toDto(Lesson lesson) {
        List<Long> studentIds = lessonParticipantRepository.findByLesson(lesson)
                .stream()
                .map(lp -> lp.getStudent().getId())
                .toList();

        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .subject(lesson.getSubject())
                .teacherId(lesson.getTeacher() != null ? lesson.getTeacher().getId() : null)
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .type(lesson.getType() != null ? lesson.getType().name() : null)
                .status(lesson.getStatus() != null ? lesson.getStatus().name() : null)
                .studentIds(studentIds)
                .build();
    }
}
