# Postman checks

Base URL: `http://localhost:8080`

## 1) Auth (public)

### POST `/api/auth/send-sms`
Body:
```json
{
  "phone": "+79991234567"
}
```

### POST `/api/auth/verify`
Body:
```json
{
  "phone": "+79991234567",
  "code": "1234"
}
```
Save `token` from response as `studentToken`.

### POST `/api/auth/verify-teacher`
Body:
```json
{
  "phone": "+79990001122",
  "code": "1234"
}
```
Save `token` as `teacherToken`.

### GET `/api/auth/validate`
Headers:
- `Authorization: Bearer {{studentToken}}`

## 2) User Service

### GET `/api/users/me`
Headers:
- `Authorization: Bearer {{studentToken}}`

### PUT `/api/users/me`
Headers:
- `Authorization: Bearer {{studentToken}}`
  Body:
```json
{
  "name": "Ivan",
  "avatar": "https://example.com/avatar.png"
}
```

### DELETE `/api/users/me`
Headers:
- `Authorization: Bearer {{studentToken}}`

### POST `/api/users/register` (ADMIN only)
Headers:
- `Authorization: Bearer {{adminToken}}`
  Body:
```json
{
  "phone": "+79995556677",
  "name": "New Student"
}
```

### PATCH `/api/users/{id}/role` (ADMIN only)
Headers:
- `Authorization: Bearer {{adminToken}}`
  Body:
```json
{
  "role": "TEACHER"
}
```
Allowed: `STUDENT`, `TEACHER`.

## 3) Lesson Service
Для админа нужно передавать teacherId.
Для учителя teacherId можно не передавать — он автоматически ставится сам.
Пример запроса для админа:

{
  "title": "Math",
  "subject": "Algebra",
  "teacherId": 5,
  "startTime": "2026-03-11T14:00:00",
  "endTime": "2026-03-11T15:00:00",
  "studentId": 2,
  "type": "INDIVIDUAL"
}
### POST `/api/lessons` (TEACHER/ADMIN)
Headers:
- `Authorization: Bearer {{teacherToken}}`
  Body:
```json
{
  "title": "Math",
  "subject": "Algebra",
  "startTime": "2026-03-11T14:00:00",
  "endTime": "2026-03-11T15:00:00",
  "studentId": 2,
  "type": "INDIVIDUAL"
}
```

### GET `/api/lessons/schedule?from=2026-03-10T00:00:00&to=2026-03-20T23:59:59`
Headers:
- `Authorization: Bearer {{studentToken}}`

### PUT `/api/lessons/{lessonId}` (TEACHER/ADMIN)
Headers:
- `Authorization: Bearer {{teacherToken}}`
  Body:
```json
{
  "title": "Math updated",
  "startTime": "2026-03-11T15:00:00",
  "endTime": "2026-03-11T16:00:00"
}
```

### PATCH `/api/lessons/{lessonId}/status` (TEACHER/ADMIN)
Headers:
- `Authorization: Bearer {{teacherToken}}`
  Body:
```json
{
  "status": "COMPLETED"
}
```
Allowed statuses:
- `PLANNED`
- `COMPLETED`
- `TEACHER_ABSENT`
- `STUDENT_ABSENT`
- `CANCELLED`

## 4) Balance Service

### GET `/api/balance/me`
Headers:
- `Authorization: Bearer {{studentToken}}`

### POST `/api/balance/{userId}/credit` (ADMIN only)
Headers:
- `Authorization: Bearer {{adminToken}}`
  Body:
```json
{
  "amount": 5,
  "comment": "Package purchase"
}
```

### POST `/api/balance/{userId}/debit` (ADMIN only)
Headers:
- `Authorization: Bearer {{adminToken}}`
  Body:
```json
{
  "amount": 1,
  "comment": "Manual deduction"
}
```

### GET `/api/balance/{userId}/history` (ADMIN only)
Headers:
- `Authorization: Bearer {{adminToken}}`

## 5) Cancellation Service

### POST `/api/cancellations/lessons/{lessonId}`
Headers:
- `Authorization: Bearer {{studentToken}}`

Rule:
- `< 10 hours` before start -> `DEBIT 1`
- `> 10 hours` before start -> `CREDIT 1`

## Swagger
- UI: `http://localhost:8080/swagger-ui.html`
- JSON: `http://localhost:8080/v3/api-docs`
