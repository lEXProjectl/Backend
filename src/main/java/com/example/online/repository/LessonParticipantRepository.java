package com.example.online.repository;

import com.example.online.entity.Lesson;
import com.example.online.entity.LessonParticipant;
import com.example.online.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LessonParticipantRepository extends JpaRepository<LessonParticipant, Long> {

    List<LessonParticipant> findByStudent(User student);

    List<LessonParticipant> findByStudentAndLessonStartTimeBetweenOrderByLessonStartTimeAsc(User student, LocalDateTime from, LocalDateTime to);

    List<LessonParticipant> findByLesson(Lesson lesson);

    void deleteByLesson(Lesson lesson);
}
