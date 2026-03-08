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

    @Value("${sms.message.template:Ваш код подтверждения: {code}}")
    private String smsMessageTemplate;

    /**
     * Генерирует и отправляет SMS код на указанный номер телефона
     */
    @Transactional
    public String generateAndSendCode(String phone) {
        // Нормализуем номер телефона
        String normalizedPhone = normalizePhoneNumber(phone);

        // Инвалидируем все предыдущие неиспользованные коды для этого номера
        smsCodeRepository.markAllAsUsedByPhone(normalizedPhone);

        // Генерируем новый код
        String code = generateCode();

        // Сохраняем код в базе данных
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

        // Отправляем SMS
        if (smsSendingEnabled) {
            sendSms(normalizedPhone, code);
        } else {
            log.info("SMS отправка отключена. Код для {}: {}", normalizedPhone, code);
        }

        return code;
    }

    /**
     * Проверяет SMS код
     */
    @Transactional
    public boolean verifyCode(String phone, String code) {
        Optional<SmsCode> smsCodeOpt = smsCodeRepository
                .findTopByPhoneAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(phone, LocalDateTime.now());

        if (smsCodeOpt.isEmpty()) {
            return false;
        }

        SmsCode smsCode = smsCodeOpt.get();

        // Проверяем количество попыток
        if (smsCode.getAttempts() >= maxAttempts) {
            smsCode.setUsed(true);
            smsCodeRepository.save(smsCode);
            return false;
        }

        // Увеличиваем счетчик попыток
        smsCode.setAttempts(smsCode.getAttempts() + 1);

        // Проверяем код
        if (smsCode.getCode().equals(code)) {
            smsCode.setUsed(true);
            smsCodeRepository.save(smsCode);
            return true;
        }

        smsCodeRepository.save(smsCode);
        return false;
    }

    /**
     * Генерирует случайный числовой код
     */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * Отправляет SMS через выбранный sender (Twilio или mock)
     */
    private void sendSms(String phone, String code) {
        String message = smsMessageTemplate.replace("{code}", code);
        smsSender.send(phone, message);
    }

    /**
     * Форматирует номер телефона для Twilio (добавляет + если отсутствует)
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("Номер телефона не может быть пустым");
        }
        // Twilio требует формат E.164 (начинается с +)
        if (!phone.startsWith("+")) {
            // Если номер начинается с 8 (российский формат), заменяем на +7
            if (phone.startsWith("8")) {
                return "+7" + phone.substring(1);
            }
            // Иначе просто добавляем +
            return "+" + phone;
        }
        return phone;
    }

    /**
     * Очищает истекшие коды (запускается по расписанию)
     */
    @Scheduled(fixedRate = 3600000) // Каждый час
    @Transactional
    public void cleanupExpiredCodes() {
        smsCodeRepository.deleteExpiredCodes(LocalDateTime.now());
        log.debug("Очистка истекших SMS кодов выполнена");
    }
}

