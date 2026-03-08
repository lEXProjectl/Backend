package com.example.online;

import com.example.online.sms.MockSmsSender;
import com.example.online.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "twilio.enabled=false",
        "sms.send.enabled=true",
        "sms.code.length=4",
        "sms.message.template=Ваш код подтверждения: {code}"
})
class SmsServiceMockSenderTest {

    @Autowired
    SmsService smsService;

    @Autowired
    MockSmsSender mockSmsSender;

    @Test
    void generateAndSendCode_sendsMessageViaMockSender() {
        String phone = "89991234567";

        String code = smsService.generateAndSendCode(phone);

        assertThat(code).hasSize(4);
        assertThat(mockSmsSender.getLastTo()).isEqualTo("+79991234567");
        assertThat(mockSmsSender.getLastMessage()).contains(code);
    }
}


