package com.example.online.repository;

import com.example.online.entity.Request;
import com.example.online.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByTeacher(User teacher);

}