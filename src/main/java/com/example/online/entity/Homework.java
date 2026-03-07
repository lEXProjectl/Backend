package com.example.online.entity;

import com.example.online.entity.enums.HomeworkStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "homeworks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Homework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String subject;

    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private HomeworkStatus status;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;
}