# API Contract Documentation
## Frontend Integration Guide for Backend Team

**Project:** Klinik HUB PKP  
**Frontend Framework:** React 19 + TypeScript  
**Frontend Location:** `/Klinik_HUB_PKP/src/api/`  
**Status:** Ready for Backend Implementation  
**Date:** February 23, 2026

---

## 📋 Overview

This document defines the API contract between the **Frontend (React)** and **Backend (Spring Boot)**.

The frontend has implemented:
- ✅ **Request/Response type definitions** (`src/api/types.ts`)
- ✅ **Idempotency layer** (`src/api/idempotency.ts`) - prevents duplicate requests
- ✅ **API service layer** (`src/api/service.ts`) - with mock/real toggles
- ✅ **Custom hooks** (`src/api/hooks.ts`) - for easy component integration
- ✅ **Authentication context** (`src/Warga/components/Auth/authContext.tsx`) - JWT support

The backend should implement these endpoints exactly as specified below.

---

## 🔐 Authentication

### Header Format
All requests (except `/auth/login` and `/auth/register`) must include:
```http
Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: <UNIQUE_REQUEST_ID>  (required for state-changing operations)
```

### JWT Token
- **Issued by:** `POST /api/auth/login`
- **Expiration:** Recommend 24 hours minimum
- **Payload:** Must include `sub` (user ID), `role` (Warga/Balai), `email`

---

## 🚀 Endpoints Specification

### BASE URL
```
http://localhost:8080/api
```

---

## 🔑 AUTH ENDPOINTS

### 1. **Login**
```http
POST /api/auth/login
Content-Type: application/json

Request:
{
  "username": "string",      // email or username
  "password": "string",
  "role": "Warga" | "Balai"
}

Response 200 OK:
{
  "token": "string",         // JWT token
  "user": {
    "id": "string",          // Unique user ID
    "username": "string",
    "email": "string",
    "role": "Warga" | "Balai",
    "fullName": "string",
    "unit": "string"         // Optional: Balai's unit name (for Balai role)
  }
}

Response 401 Unauthorized:
{
  "error": "INVALID_CREDENTIALS",
  "message": "Username or password is incorrect",
  "statusCode": 401
}
```

### 2. **Register**
```http
POST /api/auth/register
Content-Type: application/json

Request:
{
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string",
  "phone": "string",         // Optional
  "role": "Warga" | "Balai",
  "photo": "string"          // Optional: photo URL or base64
}

Response 200 OK:
{
  "message": "User registered successfully",
  "username": "string",
  "email": "string",
  "role": "Warga" | "Balai"
}

Response 400 Bad Request:
{
  "error": "USERNAME_EXISTS",
  "message": "Username already exists",
  "statusCode": 400
}
```

---

## 📅 CONSULTATION ENDPOINTS (WARGA/User)

### 3. **Book Consultation**
```http 
POST /api/consultations/book
Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: <UNIQUE_REQUEST_ID>
Content-Type: application/json

Request:
{
  "balaiId": "string",
  "date": "string",          // ISO 8601: YYYY-MM-DD
  "timeSlot": "string",      // Format: "HH.MM - HH.MM" e.g., "09.00 - 10.00"
  "topic": "string",         // e.g., "Struktur Bangunan"
  "description": "string"    // Optional
}

Response 201 Created:
{
  "id": "string",            // Consultation ID
  "status": "menunggu_konfirmasi",
  "message": "Consultation booked successfully. Waiting for Balai confirmation.",
  "consultation": {
    "id": "string",
    "user": {
      "id": "string",
      "name": "string",
      "phone": "string",
      "email": "string",
      "avatar": "string"     // URL
    },
    "balaiLocation": "string",
    "balaiId": "string",
    "date": "string",        // YYYY-MM-DD
    "timeSlot": "string",
    "status": "menunggu_konfirmasi",
    "topic": "string",
    "description": "string",
    "createdAt": "string",   // ISO 8601 timestamp
    "updatedAt": "string"    // ISO 8601 timestamp
  }
}

Response 409 Conflict (Idempotency):
{
  "error": "REQUEST_DUPLICATE",
  "message": "Same request already being processed",
  "statusCode": 409,
  "idempotencyKey": "string",
  "status": "PROCESSING"
}
```

### 4. **Get My Consultations**
```http
GET /api/consultations/me?status=all&page=0&size=10
Authorization: Bearer <JWT_TOKEN>

Query Parameters:
- status: "menunggu_konfirmasi" | "terkonfirmasi" | "sedang_berjalan" | "selesai" | "dibatalkan" | "all"
- page: Integer (0-indexed)
- size: Integer (default: 10)

Response 200 OK:
[
  {
    "id": "string",
    "user": { ... },
    "balaiLocation": "string",
    "date": "string",
    "timeSlot": "string",
    "status": "string",
    "consultationId": "string",  // Optional: verification ID
    "topic": "string",
    "description": "string",
    "createdAt": "string",
    "updatedAt": "string"
  }
]
```

### 5. **Verify Consultation**
```http
POST /api/consultations/{consultationId}/verify
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

Request:
{
  "verificationCode": "string"  // The verification code from Balai
}

Response 200 OK:
{
  "verified": true,
  "message": "Consultation verified successfully",
  "consultation": { ... }
}

Response 400 Bad Request:
{
  "error": "INVALID_VERIFICATION_CODE",
  "message": "Verification code is incorrect",
  "statusCode": 400
}
```

---

## 📊 CONSULTATION ENDPOINTS (BALAI/Admin)

### 6. **Get Dashboard Consultations**
```http
GET /api/dashboard/consultations?date=YYYY-MM-DD&balaiId=X&status=all
Authorization: Bearer <JWT_TOKEN>

Query Parameters:
- date: "string" (ISO 8601: YYYY-MM-DD, optional)
- balaiId: "string" (filter by Balai, required)
- status: "menunggu_konfirmasi" | "terkonfirmasi" | "all" (optional)
- page: Integer (0-indexed, optional)
- size: Integer (default: 20, optional)

Response 200 OK:
[
  {
    "id": "string",
    "user": { ... },
    "balaiLocation": "string",
    "date": "string",
    "timeSlot": "string",
    "status": "menunggu_konfirmasi",
    "topic": "string",
    "description": "string",
    "createdAt": "string",
    "updatedAt": "string"
  }
]
```

### 7. **Confirm Consultation**
```http
PUT /api/consultations/{consultationId}/confirm
Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: <UNIQUE_REQUEST_ID>
Content-Type: application/json

Request:
{
  "idempotencyKey": "string",  // Must match header
  "notes": "string"             // Optional
}

Response 200 OK:
{
  "id": "string",
  "status": "terkonfirmasi",
  "message": "Consultation confirmed successfully",
  "consultation": {
    "id": "string",
    "status": "terkonfirmasi",
    ...
  }
}

Response 409 Conflict (Duplicate Request):
{
  "error": "DUPLICATE_REQUEST",
  "message": "This confirmation was already processed",
  "statusCode": 409,
  "idempotencyKey": "string",
  "status": "PROCESSING" | "SUCCESS"
}

Response 409 Conflict (Double Confirm):
{
  "error": "INVALID_STATE_TRANSITION",
  "message": "Consultation is already confirmed",
  "statusCode": 409
}
```

### 8. **Reject/Cancel Consultation**
```http
PUT /api/consultations/{consultationId}/reject
Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: <UNIQUE_REQUEST_ID>
Content-Type: application/json

Request:
{
  "idempotencyKey": "string",  // Must match header
  "reason": "string"            // Cancellation reason
}

Response 200 OK:
{
  "id": "string",
  "status": "dibatalkan",
  "message": "Consultation rejected successfully",
  "consultation": {
    "id": "string",
    "status": "dibatalkan",
    "cancellationReason": "string",
    ...
  }
}

Response 409 Conflict (Already Rejected):
{
  "error": "INVALID_STATE_TRANSITION",
  "message": "Consultation is already cancelled",
  "statusCode": 409
}
```

### 9. **Complete Consultation**
```http
PUT /api/consultations/{consultationId}/complete
Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: <UNIQUE_REQUEST_ID>
Content-Type: application/json

Request:
{
  "idempotencyKey": "string",     // Must match header
  "verificationId": "string"      // The verification code given to the user
}

Response 200 OK:
{
  "id": "string",
  "status": "selesai",
  "message": "Consultation completed successfully",
  "consultation": {
    "id": "string",
    "status": "selesai",
    "consultationId": "string",    // Same as verificationId
    ...
  }
}

Response 409 Conflict (Already Completed):
{
  "error": "INVALID_STATE_TRANSITION",
  "message": "Consultation is already completed",
  "statusCode": 409
}
```

---

## 🏢 BALAI ENDPOINTS

### 10. **Get Balai List**
```http
GET /api/balai/list

Response 200 OK:
[
  {
    "id": "string",
    "name": "string",          // e.g., "Balai Jawa 1"
    "location": "string",      // e.g., "Jakarta"
    "phone": "string",
    "email": "string",         // Optional
    "address": "string",       // Optional
    "workingHours": {          // Optional
      "start": "string",       // "07.00"
      "end": "string"          // "16.00"
    }
  }
]
```

### 11. **Get Available Slots**
```http
GET /api/balai/{balaiId}/available-slots?date=YYYY-MM-DD
Authorization: Bearer <JWT_TOKEN>

Query Parameters:
- date: "string" (ISO 8601: YYYY-MM-DD, required)

Response 200 OK:
[
  {
    "timeSlot": "string",      // "09.00 - 10.00"
    "isAvailable": boolean,
    "bookedCount": integer,    // How many are booked
    "maxCapacity": integer     // Max capacity for this slot
  },
  {
    "timeSlot": "11.00 - 12.00",
    "isAvailable": false,
    "bookedCount": 5,
    "maxCapacity": 5
  }
]
```

---

## 📈 DASHBOARD STATS

### 12. **Get Dashboard Stats**
```http
GET /api/dashboard/stats?balaiId=X
Authorization: Bearer <JWT_TOKEN>

Query Parameters:
- balaiId: "string" (required)

Response 200 OK:
{
  "todayTotal": integer,        // Total consultations today
  "todayPending": integer,      // Status: menunggu_konfirmasi
  "todayConfirmed": integer,    // Status: terkonfirmasi
  "todayCompleted": integer,    // Status: selesai
  "todayRejected": integer      // Status: dibatalkan
}
```

---

## 🛡️ Idempotency Implementation

### What is Idempotency?
Prevents duplicate requests from causing duplicate state changes.

### When It's Needed
All **state-changing operations** must include `Idempotency-Key` header:
- ✅ `POST /api/consultations/book`
- ✅ `PUT /api/consultations/{id}/confirm`
- ✅ `PUT /api/consultations/{id}/reject`
- ✅ `PUT /api/consultations/{id}/complete`

### How to Implement (Backend)

1. **Create Idempotency Store** (Database table):
```sql
CREATE TABLE idempotency_store (
  id UUID PRIMARY KEY,
  idempotency_key VARCHAR(255) UNIQUE NOT NULL,
  user_id VARCHAR(255),
  endpoint VARCHAR(255),
  request_body TEXT,
  response_body TEXT,
  status ENUM('PROCESSING', 'SUCCESS', 'FAILED'),
  created_at TIMESTAMP,
  ttl TIMESTAMP,  -- Expire after 24 hours
  INDEX idx_key (idempotency_key),
  INDEX idx_ttl (ttl)
);
```

2. **Before Processing Request**:
```java
// Pseudo-code
@Interceptor
public ResponseEntity<?> checkIdempotency(String idempotencyKey) {
  var record = idempotencyStore.findByKey(idempotencyKey);
  
  if (record == null) {
    // New request: mark as PROCESSING, store request body
    store(PROCESSING, idempotencyKey, requestBody);
    return processRequest();
  }
  
  if (record.status == PROCESSING) {
    // Return 409 Conflict - still processing
    return ResponseEntity.status(409)
      .body(new IdempotencyError("Request already in progress"));
  }
  
  if (record.status == SUCCESS) {
    // Return cached response
    return ResponseEntity.ok(record.responseBody);
  }
  
  if (record.status == FAILED) {
    // Retry the request or return error
    return processRequest();
  }
}
```

3. **After Processing**:
```java
// Mark as SUCCESS and store response
idempotencyStore.update(idempotencyKey, SUCCESS, responseBody);
```

4. **Cleanup Job** (runs daily):
```java
// Delete records older than 24 hours
idempotencyStore.deleteWhere("ttl < NOW()");
```

---

## 📝 Error Handling

### Standard Error Response
```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "statusCode": 400,
  "timestamp": "2026-02-23T10:30:00Z"
}
```

### Common Errors
| Code | Status | Message | Action |
|------|--------|---------|--------|
| `INVALID_CREDENTIALS` | 401 | Login failed | Retry or reset password |
| `TOKEN_EXPIRED` | 401 | JWT token expired | Re-login |
| `UNAUTHORIZED` | 403 | User role mismatch | Check access level |
| `CONSULTATION_NOT_FOUND` | 404 | Consultation doesn't exist | Refresh list |
| `INVALID_STATE_TRANSITION` | 409 | Can't confirm already confirmed | Check consultation status |
| `DUPLICATE_REQUEST` | 409 | Request already processing | Wait or retry with same key |
| `SLOT_UNAVAILABLE` | 409 | Time slot is booked | Pick another time |

---

## 🔄 State Machine (Consultation Status)

```
menunggu_konfirmasi
    ↓
    ├─→ terkonfirmasi
    │       ↓
    │   sedang_berjalan (auto when time reaches)
    │       ↓
    │   selesai (Balai confirms completion)
    │
    └─→ dibatalkan (Balai or Warga cancels)
```

**Valid Transitions:**
- `menunggu_konfirmasi` → `terkonfirmasi` (Balai confirms)
- `menunggu_konfirmasi` → `dibatalkan` (Balai rejects)
- `terkonfirmasi` → `sedang_berjalan` (Auto when time reached)
- `sedang_berjalan` → `selesai` (Balai completes)
- `terkonfirmasi` → `dibatalkan` (Balai cancels)

---

## 🚦 Testing Checklist

- [ ] All endpoints return correct status codes
- [ ] JWT validation works on protected endpoints
- [ ] Idempotency prevents duplicate state changes
- [ ] Error responses match the spec
- [ ] Consultation state transitions are enforced
- [ ] Balai can only see their own consultations
- [ ] Warga can only see their own consultations
- [ ] Time slots validation works
- [ ] TTL cleanup removes old idempotency records

---

## 🔗 Frontend Integration

### Switching from Mock to Real Backend

In `src/api/service.ts`, change:
```typescript
const USE_MOCK = true;  // ← Change to FALSE
```

No other code changes needed! The frontend will automatically:
1. Use real API endpoints
2. Include authentication headers
3. Handle idempotency keys
4. Parse responses as specified

### Example Component Usage

```tsx
import { useConfirmConsultation } from './api/hooks';

export function ConsultationCard({ consultation, balaiId }) {
  const { confirm, loading, error } = useConfirmConsultation();
  
  const handleConfirm = async () => {
    try {
      const result = await confirm(balaiId, consultation.id);
      console.log('Confirmed:', result);
    } catch (err) {
      console.error('Failed:', err.message);
    }
  };
  
  return (
    <button onClick={handleConfirm} disabled={loading}>
      {loading ? 'Confirming...' : 'Confirm'}
    </button>
  );
}
```

---

## 📞 Contact & Questions

For questions about this API contract, refer to:
- Frontend Types: `src/api/types.ts`
- Frontend Service: `src/api/service.ts`
- Mock Data: `src/Balai/data/mockData.ts`

---

**Version:** 1.0  
**Last Updated:** February 23, 2026  
**Status:** Ready for Backend Implementation
