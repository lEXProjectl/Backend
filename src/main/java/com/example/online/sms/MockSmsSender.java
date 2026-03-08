package com.example.online.sms;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Мок-отправитель SMS.
 * Активен, когда twilio.enabled=false (по умолчанию).
 * Сохраняет последнее отправленное SMS в памяти и логирует его.
 */
@Component
@ConditionalOnProperty(name = "twilio.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
@Getter
public class MockSmsSender implements SmsSender {

    private volatile String lastTo;
    private volatile String lastMessage;

    @Override
    public void send(String toPhoneE164, String message) {
        this.lastTo = toPhoneE164;
        this.lastMessage = message;
        log.info("[МОК SMS] номер={} сообщение={}", toPhoneE164, message);
    }
}



