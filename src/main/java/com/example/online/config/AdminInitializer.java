package com.example.online.config;

import com.example.online.entity.User;
import com.example.online.entity.enums.Role;
import com.example.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * Создаёт мок-админа при старте приложения, если его ещё нет.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final UserRepository userRepository;

    @Value("${admin.phone:+79990000000}")
    private String adminPhone;

    @Value("${admin.name:Admin}")
    private String adminName;

    @PostConstruct
    public void initAdmin() {
        userRepository.findByPhone(adminPhone).ifPresentOrElse(existing -> {
            log.info("Админ с телефоном {} уже существует", adminPhone);
        }, () -> {
            User admin = User.builder()
                    .phone(adminPhone)
                    .name(adminName)
                    .role(Role.ADMIN)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            log.info("Тестовый админ создан с телефоном {}", adminPhone);
        });
    }
}


