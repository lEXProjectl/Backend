package com.example.online.service;

import com.example.online.dto.auth.cancellation.CancelLessonResponse;
import com.example.online.entity.Lesson;
import com.example.online.entity.LessonParticipant;
import com.example.online.entity.User;
import com.example.online.entity.enums.LessonStatus;
import com.example.online.entity.enums.Role;
import com.example.online.repository.LessonParticipantRepository;
import com.example.online.repository.LessonRepository;
import com.example.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CancellationService {

    private final LessonRepository lessonRepository;
    private final LessonParticipantRepository lessonParticipantRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;

    @Value("${cancellation.rule.hours:10}")
    private long cancellationHours;

    @Transactional
    public CancelLessonResponse cancelLesson(Long lessonId, String actorPhone) {
        User actor = userRepository.findByPhone(actorPhone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        if (lesson.getStatus() == LessonStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson already cancelled");
        }
        if (lesson.getStatus() == LessonStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completed lesson cannot be cancelled");
        }
        if (lesson.getStartTime() == null || !lesson.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only future lessons can be cancelled");
        }

        List<LessonParticipant> participants = lessonParticipantRepository.findByLesson(lesson);

        if (actor.getRole() == Role.STUDENT) {
            LessonParticipant participation = participants.stream()
                    .filter(lp -> lp.getStudent() != null && lp.getStudent().getId().equals(actor.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Student is not lesson participant"));

            long hoursBeforeStart = Duration.between(LocalDateTime.now(), lesson.getStartTime()).toHours();
            boolean penalty = applyStudentCancellationPolicy(participation.getStudent(), hoursBeforeStart);

            lesson.setStatus(LessonStatus.CANCELLED);
            lessonRepository.save(lesson);

            return CancelLessonResponse.builder()
                    .lessonId(lesson.getId())
                    .lessonStatus(lesson.getStatus().name())
                    .balanceChanged(true)
                    .balanceAction(penalty ? "DEBIT" : "CREDIT")
                    .message(penalty
                            ? "������ ����� ��� �� " + cancellationHours + " �����: ������� 1 �������"
                            : "������ ����� ��� �� " + cancellationHours + " �����: ���������� 1 �������")
                    .build();
        }

        if (actor.getRole() == Role.TEACHER) {
            if (lesson.getTeacher() == null || !lesson.getTeacher().getId().equals(actor.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher can cancel only own lessons");
            }
        }

        lesson.setStatus(LessonStatus.CANCELLED);
        lessonRepository.save(lesson);

        return CancelLessonResponse.builder()
                .lessonId(lesson.getId())
                .lessonStatus(lesson.getStatus().name())
                .balanceChanged(false)
                .balanceAction("NONE")
                .message("���� �������")
                .build();
    }

    private boolean applyStudentCancellationPolicy(User student, long hoursBeforeStart) {
        if (hoursBeforeStart < cancellationHours) {
            balanceService.debit(student.getId(), 1, "Cancellation penalty: less than " + cancellationHours + " hours");
            return true;
        }

        balanceService.credit(student.getId(), 1, "Cancellation refund: more than " + cancellationHours + " hours");
        return false;
    }
}
