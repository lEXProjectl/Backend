package com.example.online.repository;

import com.example.online.entity.SmsCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SmsCodeRepository extends JpaRepository<SmsCode, Long> {

    Optional<SmsCode> findTopByPhoneAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String phone, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM SmsCode s WHERE s.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE SmsCode s SET s.used = true WHERE s.phone = :phone AND s.used = false")
    void markAllAsUsedByPhone(@Param("phone") String phone);
}

