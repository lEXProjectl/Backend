package com.example.online.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lesson_participants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;
}