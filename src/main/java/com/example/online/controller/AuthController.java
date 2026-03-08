package com.example.online.controller;

import com.example.online.dto.auth.AuthResponse;
import com.example.online.dto.auth.SendSmsRequest;
import com.example.online.dto.auth.SendSmsResponse;
import com.example.online.dto.auth.VerifySmsRequest;
import com.example.online.entity.User;
import com.example.online.entity.enums.Role;
import com.example.online.repository.UserRepository;
import com.example.online.service.JwtService;
import com.example.online.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SmsService smsService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Отправка SMS кода на номер телефона
     */
    @PostMapping("/send-sms")
    public ResponseEntity<SendSmsResponse> sendSms(@Valid @RequestBody SendSmsRequest request) {
        try {
            smsService.generateAndSendCode(request.getPhone());
            return ResponseEntity.ok(new SendSmsResponse("SMS код отправлен", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SendSmsResponse("Ошибка при отправке SMS: " + e.getMessage(), false));
        }
    }

    /**
     * Верификация SMS кода и вход в систему
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyAndLogin(@Valid @RequestBody VerifySmsRequest request) {
        // Проверяем SMS-код
        boolean isValid = smsService.verifyCode(request.getPhone(), request.getCode());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверный или истекший код"));
        }

        // Ищем или создаем пользователя (ученика)
        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    // Создаем нового пользователя, если его нет
                    User newUser = User.builder()
                            .phone(request.getPhone())
                            .role(Role.STUDENT) // По умолчанию STUDENT
                            .createdAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(newUser);
                });

        // Генерируем JWT токен
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getPhone());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());

        String token = jwtService.generateToken(userDetails.getUsername(), extraClaims);

        // Возвращаем ответ с токеном
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .phone(user.getPhone())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Отдельный поток регистрации/входа для преподавателя.
     * Создаёт пользователя с ролью TEACHER, если такого телефона ещё нет.
     * Если пользователь уже существует с другой ролью, возвращает ошибку.
     */
    @PostMapping("/verify-teacher")
    public ResponseEntity<?> verifyAndLoginTeacher(@Valid @RequestBody VerifySmsRequest request) {
        // Проверяем SMS-код
        boolean isValid = smsService.verifyCode(request.getPhone(), request.getCode());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверный или истекший код"));
        }

        // Проверяем, не зарегистрирован ли уже этот номер
        User user = userRepository.findByPhone(request.getPhone()).orElse(null);
        if (user != null && user.getRole() != Role.TEACHER) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Пользователь с этим номером уже зарегистрирован",
                            "role", user.getRole().name()
                    ));
        }

        if (user == null) {
            // Создаём нового преподавателя
            user = User.builder()
                    .phone(request.getPhone())
                    .role(Role.TEACHER)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);
        }

        // Генерируем JWT токен
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getPhone());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());

        String token = jwtService.generateToken(userDetails.getUsername(), extraClaims);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .phone(user.getPhone())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Проверка валидности токена
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Токен отсутствует"));
        }

        String token = authHeader.substring(7);

        try {
            String phone = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(phone);

            boolean isValid = jwtService.validateToken(token, userDetails);

            if (isValid) {
                return ResponseEntity.ok(Map.of("valid", true, "phone", phone));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "error", "Токен недействителен"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Ошибка при проверке токена: " + e.getMessage()));
        }
    }
}

