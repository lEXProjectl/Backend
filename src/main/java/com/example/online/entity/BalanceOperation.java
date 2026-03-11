package com.example.online.entity;

import com.example.online.entity.enums.BalanceOperationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "balance_operations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_id", nullable = false)
    private UserBalance balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BalanceOperationType type;

    @Column(nullable = false)
    private Integer amount;

    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
