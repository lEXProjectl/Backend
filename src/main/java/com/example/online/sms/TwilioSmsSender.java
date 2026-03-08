package com.example.online.sms;

import com.twilio.Twilio;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Реальная отправка SMS через Twilio.
 * Включается свойством twilio.enabled=true.
 */
@Component
@ConditionalOnProperty(name = "twilio.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class TwilioSmsSender implements SmsSender {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.phone.number:}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        if (accountSid == null || accountSid.isBlank() || authToken == null || authToken.isBlank()) {
            throw new IllegalStateException("twilio.enabled=true, но не заданы twilio.account.sid / twilio.auth.token");
        }
        Twilio.init(accountSid, authToken);
        log.info("TwilioSmsSender инициализирован");
    }

    @Override
    public void send(String toPhoneE164, String message) {
        if (fromPhoneNumber == null || fromPhoneNumber.isBlank()) {
            throw new IllegalStateException("Не задан twilio.phone.number");
        }
        try {
            Message twilioMessage = Message.creator(
                            new PhoneNumber(toPhoneE164),
                            new PhoneNumber(fromPhoneNumber),
                            message)
                    .create();
            log.info("Twilio: SMS отправлено, номер={}, sid={}", toPhoneE164, twilioMessage.getSid());
        } catch (TwilioException e) {
            log.error("Twilio: ошибка отправки SMS на номер {}: {}", toPhoneE164, e.getMessage(), e);
            throw e;
        }
    }
}



