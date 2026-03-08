package com.example.online.sms;

public interface SmsSender {

    void send(String toPhoneE164, String message);
}


