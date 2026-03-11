package com.example.online.dto.auth.balance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceOperationResponse {

    private Long id;
    private String type;
    private Integer amount;
    private String comment;
    private LocalDateTime createdAt;
}

