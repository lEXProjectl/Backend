package com.example.online.repository;

import com.example.online.entity.Lesson;
import com.example.online.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByTeacher(User teacher);

}