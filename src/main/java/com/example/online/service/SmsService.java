package com.example.online.service;

import com.example.online.entity.SmsCode;
import com.example.online.repository.SmsCodeRepository;
import com.example.online.sms.SmsSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final SmsCodeRepository smsCodeRepository;
    private final SmsSender smsSender;

    @Value("${sms.code.length:4}")
    private int codeLength;

    @Value("${sms.code.expiration.minutes:5}")
    private int expirationMinutes;

    @Value("${sms.code.max.attempts:3}")
    private int maxAttempts;

    @Value("${sms.send.enabled:true}")
    private boolean smsSendingEnabled;

    @Value("${sms.message.template:��� ��� �������������: {code}}")
    private String smsMessageTemplate;

    @Transactional
    public String generateAndSendCode(String phone) {
        String normalizedPhone = normalizePhoneNumber(phone);
        smsCodeRepository.markAllAsUsedByPhone(normalizedPhone);

        String code = generateCode();
        LocalDateTime now = LocalDateTime.now();
        SmsCode smsCode = SmsCode.builder()
                .phone(normalizedPhone)
                .code(code)
                .createdAt(now)
                .expiresAt(now.plusMinutes(expirationMinutes))
                .used(false)
                .attempts(0)
                .build();

        smsCodeRepository.save(smsCode);

        if (smsSendingEnabled) {
            sendSms(normalizedPhone, code);
        } else {
            log.info("SMS sending is disabled. Code for {}: {}", normalizedPhone, code);
        }

        return code;
    }

    @Transactional
    public boolean verifyCode(String phone, String code) {
        String normalizedPhone = normalizePhoneNumber(phone);
        Optional<SmsCode> smsCodeOpt = smsCodeRepository
                .findTopByPhoneAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(normalizedPhone, LocalDateTime.now());

        if (smsCodeOpt.isEmpty()) {
            log.warn(" {}", normalizedPhone);
            return false;
        }

        SmsCode smsCode = smsCodeOpt.get();

        if (smsCode.getAttempts() >= maxAttempts) {
            smsCode.setUsed(true);
            smsCodeRepository.save(smsCode);
            log.warn(" {}", normalizedPhone);
            return false;
        }

        smsCode.setAttempts(smsCode.getAttempts() + 1);

        if (smsCode.getCode().equals(code)) {
            smsCode.setUsed(true);
            smsCodeRepository.save(smsCode);
            return true;
        }

        smsCodeRepository.save(smsCode);
        log.warn(" {}", normalizedPhone);
        return false;
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void sendSms(String phone, String code) {
        String message = smsMessageTemplate.replace("{code}", code);
        smsSender.send(phone, message);
    }

    private String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        if (!phone.startsWith("+")) {
            if (phone.startsWith("8")) {
                return "+7" + phone.substring(1);
            }
            return "+" + phone;
        }
        return phone;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredCodes() {
        smsCodeRepository.deleteExpiredCodes(LocalDateTime.now());
        log.debug("Expired SMS codes cleanup done");
    }
}
