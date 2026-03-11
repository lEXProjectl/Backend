package com.example.online.controller;

import com.example.online.dto.auth.balance.BalanceOperationResponse;
import com.example.online.dto.auth.balance.BalanceResponse;
import com.example.online.dto.auth.balance.UpdateBalanceRequest;
import com.example.online.service.BalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/me")
    public ResponseEntity<BalanceResponse> myBalance(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(balanceService.getMyBalance(principal.getUsername()));
    }

    @PostMapping("/{userId}/credit")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BalanceResponse> credit(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateBalanceRequest request
    ) {
        return ResponseEntity.ok(balanceService.credit(userId, request.getAmount(), request.getComment()));
    }

    @PostMapping("/{userId}/debit")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BalanceResponse> debit(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateBalanceRequest request
    ) {
        return ResponseEntity.ok(balanceService.debit(userId, request.getAmount(), request.getComment()));
    }

    @GetMapping("/{userId}/history")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<BalanceOperationResponse>> history(@PathVariable Long userId) {
        return ResponseEntity.ok(balanceService.history(userId));
    }
}
