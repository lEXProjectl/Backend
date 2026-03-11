# Spring Boot 4.0.2
# Java 21
# http://localhost:8080/swagger-ui.html
## Auth по телефону + SMS

Публичные endpoints (не требуют JWT):
- `/api/auth/send-sms`
- `/api/auth/verify`
- `/api/auth/validate`

В `SecurityConfig` это включено правилом:
- `requestMatchers("/api/auth/**").permitAll()`

## Как проверить SMS (MOCK режим, без Twilio)

По умолчанию `twilio.enabled=false`, поэтому включается `MockSmsSender`:
- SMS не отправляется наружу
- сообщение логируется и хранится как “последнее отправленное” внутри бина `MockSmsSender`

### Проверка через API
1) Запусти приложение
2) Вызови:
- `POST /api/auth/send-sms` c JSON: `{"phone":"+79991234567"}`
3) В логах появится строка вида:
- `[MOCK SMS] to=+79991234567 message=Ваш код подтверждения: 1234`
4) Потом отправь код:
- `POST /api/auth/verify` c JSON: `{"phone":"+79991234567","code":"1234"}`

### Проверка через тест
Запусти тест `SmsServiceMockSenderTest` — он проверяет, что:
- номер нормализуется `8999...` → `+7999...`
- сообщение содержит сгенерированный код

## Как включить Twilio и проверить реальную отправку

### Важно про Trial аккаунт Twilio
- Trial Twilio обычно позволяет слать SMS только на **верифицированные** номера получателей.
- Проверь это в Twilio Console (Verified Caller IDs / Verified numbers).

### Настройки (через env vars — рекомендовано)
Нужно выставить:
- `TWILIO_ACCOUNT_SID`
- `TWILIO_AUTH_TOKEN`
- `TWILIO_PHONE_NUMBER` (ваш Twilio номер, формат E.164, например `+1...`)
- `TWILIO_ENABLED=true`

Пример (PowerShell):
```powershell
$env:TWILIO_ACCOUNT_SID="ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
$env:TWILIO_AUTH_TOKEN="xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
$env:TWILIO_PHONE_NUMBER="+12345678901"
$env:TWILIO_ENABLED="true"
```

После этого:
1) Запусти приложение
2) Вызови `POST /api/auth/send-sms` с номером в формате E.164 (например `+79991234567`)
3) В логах появится:
- `TwilioSmsSender initialized`
- `Twilio SMS sent to=... sid=SM...`
4) В Twilio Console → **Messaging** → **Logs** можно увидеть статус доставки по `sid`.

### Типичные проблемы
- **21608 / trial restriction**: номер получателя не верифицирован (trial).
- **Неверный формат номера**: Twilio требует E.164 (начинается с `+` и код страны).
- **Пустой from**: не задан `TWILIO_PHONE_NUMBER`.
