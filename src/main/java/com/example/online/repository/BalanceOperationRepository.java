package com.example.online.repository;

import com.example.online.entity.BalanceOperation;
import com.example.online.entity.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceOperationRepository extends JpaRepository<BalanceOperation, Long> {

    List<BalanceOperation> findByBalanceOrderByCreatedAtDesc(UserBalance balance);
}
