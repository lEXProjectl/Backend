package com.example.online.service;

import com.example.online.dto.auth.user.RegisterUserRequest;
import com.example.online.dto.auth.user.UpdateUserProfileRequest;
import com.example.online.dto.auth.user.UserResponse;
import com.example.online.entity.User;
import com.example.online.entity.enums.Role;
import com.example.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this phone already exists");
        }

        User user = User.builder()
                .phone(request.getPhone())
                .name(request.getName())
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .build();

        return toDto(userRepository.save(user));
    }

    public UserResponse getByPhone(String phone) {
        return toDto(findByPhone(phone));
    }

    public UserResponse updateProfile(String phone, UpdateUserProfileRequest request) {
        User user = findByPhone(phone);

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        return toDto(userRepository.save(user));
    }

    public void deleteAccount(String phone) {
        User user = findByPhone(phone);
        userRepository.delete(user);
    }

    public UserResponse changeRole(Long userId, String roleRaw) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Role role;
        try {
            role = Role.valueOf(roleRaw);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed roles: STUDENT, TEACHER, ADMIN");
        }

        user.setRole(role);
        return toDto(userRepository.save(user));
    }

    private User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserResponse toDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .avatar(user.getAvatar())
                .rating(user.getRating())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
