package com.example.online.dto.auth.balance;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateBalanceRequest {

    @Min(value = 1, message = "Amount should be >= 1")
    private Integer amount;

    private String comment;
}

