package com.example.online.service;

import com.example.online.dto.auth.balance.BalanceOperationResponse;
import com.example.online.dto.auth.balance.BalanceResponse;
import com.example.online.entity.BalanceOperation;
import com.example.online.entity.User;
import com.example.online.entity.UserBalance;
import com.example.online.entity.enums.BalanceOperationType;
import com.example.online.repository.BalanceOperationRepository;
import com.example.online.repository.UserBalanceRepository;
import com.example.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final UserRepository userRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final BalanceOperationRepository balanceOperationRepository;

    public BalanceResponse getMyBalance(String phone) {
        User user = findUserByPhone(phone);
        return toBalanceResponse(getOrCreateBalance(user));
    }

    @Transactional
    public BalanceResponse credit(Long userId, Integer amount, String comment) {
        User user = findUserById(userId);
        UserBalance balance = getOrCreateBalance(user);

        balance.setLessonsCount(balance.getLessonsCount() + amount);
        balance.setUpdatedAt(LocalDateTime.now());
        UserBalance savedBalance = userBalanceRepository.save(balance);

        saveOperation(savedBalance, BalanceOperationType.CREDIT, amount, comment);
        return toBalanceResponse(savedBalance);
    }

    @Transactional
    public BalanceResponse debit(Long userId, Integer amount, String comment) {
        User user = findUserById(userId);
        UserBalance balance = getOrCreateBalance(user);

        if (balance.getLessonsCount() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough lessons on balance");
        }

        balance.setLessonsCount(balance.getLessonsCount() - amount);
        balance.setUpdatedAt(LocalDateTime.now());
        UserBalance savedBalance = userBalanceRepository.save(balance);

        saveOperation(savedBalance, BalanceOperationType.DEBIT, amount, comment);
        return toBalanceResponse(savedBalance);
    }

    public List<BalanceOperationResponse> history(Long userId) {
        User user = findUserById(userId);
        UserBalance balance = getOrCreateBalance(user);

        return balanceOperationRepository.findByBalanceOrderByCreatedAtDesc(balance)
                .stream()
                .map(this::toOperationResponse)
                .toList();
    }

    private void saveOperation(UserBalance balance, BalanceOperationType type, Integer amount, String comment) {
        BalanceOperation operation = BalanceOperation.builder()
                .balance(balance)
                .type(type)
                .amount(amount)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
        balanceOperationRepository.save(operation);
    }

    private User findUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserBalance getOrCreateBalance(User user) {
        return userBalanceRepository.findByUser(user)
                .orElseGet(() -> userBalanceRepository.save(
                        UserBalance.builder()
                                .user(user)
                                .lessonsCount(0)
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));
    }

    private BalanceResponse toBalanceResponse(UserBalance balance) {
        return BalanceResponse.builder()
                .userId(balance.getUser().getId())
                .lessonsCount(balance.getLessonsCount())
                .build();
    }

    private BalanceOperationResponse toOperationResponse(BalanceOperation operation) {
        return BalanceOperationResponse.builder()
                .id(operation.getId())
                .type(operation.getType().name())
                .amount(operation.getAmount())
                .comment(operation.getComment())
                .createdAt(operation.getCreatedAt())
                .build();
    }
}
