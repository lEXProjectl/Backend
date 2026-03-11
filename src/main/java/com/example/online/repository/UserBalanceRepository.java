package com.example.online.repository;

import com.example.online.entity.User;
import com.example.online.entity.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {

    Optional<UserBalance> findByUser(User user);
}
