package com.example.online.repository;

import com.example.online.entity.Homework;
import com.example.online.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {

    List<Homework> findByStudent(User student);

}