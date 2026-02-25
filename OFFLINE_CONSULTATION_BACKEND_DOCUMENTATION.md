# Offline Consultation Scheduling — Backend Documentation

**Module:** `com.hub.app.offlineconsultation`  
**Framework:** Spring Boot 3.5.6 / Java 21  
**Database:** PostgreSQL with Liquibase migrations  
**Status:** Fully Implemented  
**Date:** February 25, 2026

---

## Table of Contents

1. [Overview](#1-overview)
2. [Package Structure](#2-package-structure)
3. [API Endpoints](#3-api-endpoints)
   - [3.1 Book Consultation (Warga)](#31-book-consultation)
   - [3.2 Get My Consultations (Warga)](#32-get-my-consultations)
   - [3.3 Verify Consultation (Warga)](#33-verify-consultation)
   - [3.4 Get Dashboard Consultations (Balai)](#34-get-dashboard-consultations)
   - [3.5 Confirm Consultation (Balai)](#35-confirm-consultation)
   - [3.6 Reject Consultation (Balai)](#36-reject-consultation)
   - [3.7 Complete Consultation (Balai)](#37-complete-consultation)
   - [3.8 Get Dashboard Stats (Balai)](#38-get-dashboard-stats)
   - [3.9 Get Balai List (Public)](#39-get-balai-list)
   - [3.10 Get Available Slots](#310-get-available-slots)
4. [State Machine](#4-state-machine)
5. [Idempotency](#5-idempotency)
6. [Security & Authorization](#6-security--authorization)
7. [Database Schema](#7-database-schema)
8. [Error Codes](#8-error-codes)
9. [Configuration Files](#9-configuration-files)

---

## 1. Overview

The Offline Consultation Scheduling feature allows citizens (**Warga**) to book in-person consultation appointments at government offices (**Balai**). Balai administrators can manage these consultations through a dashboard — confirming, rejecting, or completing them.

### Key Capabilities

- **Warga** can browse offices, check available time slots, book consultations, view their bookings, and verify completed consultations using a code
- **Balai** can view their dashboard of incoming consultations, confirm/reject/complete them, and see today's statistics
- **Idempotency** protection prevents duplicate state-changing operations
- **State machine** enforces valid consultation lifecycle transitions
- **Role-based security** restricts endpoints by user role (HOMEOWNER / BALAI)

---

## 2. Package Structure

```
com.hub.app.offlineconsultation/
├── controller/
│   ├── WargaConsultationController.java    # Warga endpoints (book, my, verify)
│   ├── BalaiDashboardController.java       # Balai endpoints (dashboard, confirm, reject, complete, stats)
│   └── BalaiInfoController.java            # Public/shared endpoints (balai list, available slots)
├── dto/
│   ├── request/
│   │   ├── BookConsultationRequest.java
│   │   ├── ConfirmConsultationRequest.java
│   │   ├── RejectConsultationRequest.java
│   │   ├── CompleteConsultationRequest.java
│   │   └── VerifyConsultationRequest.java
│   └── response/
│       ├── BookConsultationResponse.java
│       ├── ConsultationActionResponse.java
│       ├── ConsultationDetailResponse.java
│       ├── VerifyConsultationResponse.java
│       ├── BalaiListItemResponse.java
│       ├── AvailableSlotResponse.java
│       ├── DashboardStatsResponse.java
│       ├── IdempotencyConflictResponse.java
│       ├── ErrorResponse.java
│       ├── UserSummaryDto.java
│       └── WorkingHoursDto.java
├── entity/
│   ├── ConsultationStatus.java             # Status enum with state machine
│   ├── ConsultationStatusConverter.java    # JPA AttributeConverter
│   ├── OfflineConsultationEntity.java      # Main consultation entity
│   ├── BalaiEntity.java                    # Balai office entity
│   ├── IdempotencyRecord.java             # Idempotency store entity
│   └── IdempotencyStatus.java             # PROCESSING / SUCCESS / FAILED
├── repository/
│   ├── OcConsultationRepository.java
│   ├── OcBalaiRepository.java
│   └── OcIdempotencyRepository.java
├── service/
│   ├── OfflineConsultationService.java     # Core business logic
│   ├── SlotAvailabilityService.java        # Time slot availability
│   └── IdempotencyService.java            # Idempotency check/create/mark
└── config/
    └── IdempotencyCleanupScheduler.java    # Hourly TTL cleanup
```

**Modified existing files:**
- `com.hub.app.error.ErrorCode` — 8 new error codes added (OC_* prefix)
- `com.hub.app.config.SecurityConfig` — Authorization rules for all new endpoints
- `com.hub.app.HubApplication` — `@EnableScheduling` added
- `src/main/resources/db/changelog/db.changelog-master.xml` — `038-oc-schema.sql` included
- `src/main/resources/db/changelog/038-oc-schema.sql` — New migration file

---

## 3. API Endpoints

**Base URL:** `http://localhost:8080/api`

### 3.1 Book Consultation

```http
POST /api/consultations/book
Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: <UNIQUE_REQUEST_ID>
Content-Type: application/json
```

**Role:** HOMEOWNER (Warga)

**Request Body:**
```json
{
  "balaiId": "string",
  "date": "string",          // ISO 8601: YYYY-MM-DD
  "timeSlot": "string",      // Format: "HH.MM - HH.MM" e.g., "09.00 - 10.00"
  "topic": "string",
  "description": "string"    // Optional
}
```

**Response 201 Created:**
```json
{
  "id": "string",
  "status": "menunggu_konfirmasi",
  "message": "Consultation booked successfully. Waiting for Balai confirmation.",
  "consultation": {
    "id": "string",
    "user": {
      "id": "string",
      "name": "string",
      "phone": "string",
      "email": "string",
      "avatar": "string"
    },
    "balaiLocation": "string",
    "balaiId": "string",
    "date": "string",
    "timeSlot": "string",
    "status": "menunggu_konfirmasi",
    "topic": "string",
    "description": "string",
    "createdAt": "string",
    "updatedAt": "string"
  }
}
```

**Response 409 Conflict (Idempotency):**
```json
{
  "error": "REQUEST_DUPLICATE",
  "message": "Same request already being processed",
  "statusCode": 409,
  "idempotencyKey": "string",
  "status": "PROCESSING"
}
```

**Validation:**
- `balaiId`, `date`, `timeSlot`, `topic` are required (`@NotBlank`)
- Date must not be in the past
- Time slot must have available capacity (max 5 per slot)

---

### 3.2 Get My Consultations

```http
GET /api/consultations/me?status=all&page=0&size=10
Authorization: Bearer <JWT_TOKEN>
```

**Role:** HOMEOWNER (Warga)

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `status` | string | `all` | `menunggu_konfirmasi`, `terkonfirmasi`, `sedang_berjalan`, `selesai`, `dibatalkan`, or `all` |
| `page` | int | `0` | 0-indexed page number |
| `size` | int | `10` | Page size |

**Response 200 OK:**
```json
[
  {
    "id": "string",
    "user": { "id": "string", "name": "string", "phone": "string", "email": "string", "avatar": "string" },
    "balaiLocation": "string",
    "balaiId": "string",
    "date": "string",
    "timeSlot": "string",
    "status": "string",
    "consultationId": "string",
    "topic": "string",
    "description": "string",
    "createdAt": "string",
    "updatedAt": "string"
  }
]
```

---

### 3.3 Verify Consultation

```http
POST /api/consultations/{consultationId}/verify
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Role:** HOMEOWNER (Warga)

**Request Body:**
```json
{
  "verificationCode": "string"
}
```

**Response 200 OK:**
```json
{
  "verified": true,
  "message": "Consultation verified successfully",
  "consultation": { ... }
}
```

**Response 400 Bad Request:**
```json
{
  "error": "INVALID_VERIFICATION_CODE",
  "message": "Verification code is incorrect",
  "statusCode": 400
}
```

**Security:** Only the owning Warga can verify their own consultation.

---

### 3.4 Get Dashboard Consultations

```http
GET /api/dashboard/consultations?balaiId=X&date=YYYY-MM-DD&status=all&page=0&size=20
Authorization: Bearer <JWT_TOKEN>
```

**Role:** BALAI

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `balaiId` | string | *required* | UUID of the Balai office |
| `date` | string | *optional* | ISO 8601 date filter (YYYY-MM-DD) |
| `status` | string | `all` | `menunggu_konfirmasi`, `terkonfirmasi`, or `all` |
| `page` | int | `0` | 0-indexed page number |
| `size` | int | `20` | Page size |

**Response 200 OK:** Array of `ConsultationDetailResponse` (same shape as section 3.2).

---

### 3.5 Confirm Consultation

```http
PUT /api/consultations/{consultationId}/confirm
Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: <UNIQUE_REQUEST_ID>
Content-Type: application/json
```

**Role:** BALAI

**Request Body:**
```json
{
  "idempotencyKey": "string",  // Must match Idempotency-Key header
  "notes": "string"            // Optional
}
```

**Response 200 OK:**
```json
{
  "id": "string",
  "status": "terkonfirmasi",
  "message": "Consultation confirmed successfully",
  "consultation": { ... }
}
```

**Response 409 Conflict (Duplicate Request):**
```json
{
  "error": "DUPLICATE_REQUEST",
  "message": "This confirmation was already processed",
  "statusCode": 409,
  "idempotencyKey": "string",
  "status": "PROCESSING"
}
```

**Response 409 Conflict (Invalid Transition):**
```json
{
  "error": "INVALID_STATE_TRANSITION",
  "message": "Cannot transition from 'terkonfirmasi' to 'terkonfirmasi'",
  "statusCode": 409
}
```

**State requirement:** Current status must be `menunggu_konfirmasi`.

---

### 3.6 Reject Consultation

```http
PUT /api/consultations/{consultationId}/reject
Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: <UNIQUE_REQUEST_ID>
Content-Type: application/json
```

**Role:** BALAI

**Request Body:**
```json
{
  "idempotencyKey": "string",  // Must match Idempotency-Key header
  "reason": "string"           // Required
}
```

**Response 200 OK:**
```json
{
  "id": "string",
  "status": "dibatalkan",
  "message": "Consultation rejected successfully",
  "consultation": {
    ...
    "cancellationReason": "string"
  }
}
```

**Response 409 Conflict (Invalid Transition):**
```json
{
  "error": "INVALID_STATE_TRANSITION",
  "message": "Consultation is already cancelled",
  "statusCode": 409
}
```

**State requirement:** Current status must be `menunggu_konfirmasi` or `terkonfirmasi`.

---

### 3.7 Complete Consultation

```http
PUT /api/consultations/{consultationId}/complete
Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: <UNIQUE_REQUEST_ID>
Content-Type: application/json
```

**Role:** BALAI

**Request Body:**
```json
{
  "idempotencyKey": "string",     // Must match Idempotency-Key header
  "verificationId": "string"     // Verification code given to the citizen
}
```

**Response 200 OK:**
```json
{
  "id": "string",
  "status": "selesai",
  "message": "Consultation completed successfully",
  "consultation": {
    ...
    "consultationId": "string"    // Same as verificationId
  }
}
```

**Response 409 Conflict (Invalid Transition):**
```json
{
  "error": "INVALID_STATE_TRANSITION",
  "message": "Consultation is already completed",
  "statusCode": 409
}
```

**State requirement:** Current status must be `terkonfirmasi` or `sedang_berjalan`.  
**Note:** If current status is `terkonfirmasi`, the service auto-advances through `sedang_berjalan` → `selesai` in a single operation.

---

### 3.8 Get Dashboard Stats

```http
GET /api/dashboard/stats?balaiId=X
Authorization: Bearer <JWT_TOKEN>
```

**Role:** BALAI

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `balaiId` | string | UUID of the Balai office (required) |

**Response 200 OK:**
```json
{
  "todayTotal": 12,
  "todayPending": 3,
  "todayConfirmed": 4,
  "todayCompleted": 3,
  "todayRejected": 2
}
```

All counts are for today's date only.

---

### 3.9 Get Balai List

```http
GET /api/balai/list
```

**Role:** Public (no authentication required)

**Response 200 OK:**
```json
[
  {
    "id": "string",
    "name": "string",
    "location": "string",
    "phone": "string",
    "email": "string",
    "address": "string",
    "workingHours": {
      "start": "string",
      "end": "string"
    }
  }
]
```

**Note:** Null fields are omitted from the response (`@JsonInclude(NON_NULL)`).

---

### 3.10 Get Available Slots

```http
GET /api/balai/{balaiId}/available-slots?date=YYYY-MM-DD
Authorization: Bearer <JWT_TOKEN>
```

**Role:** Any authenticated user

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `date` | string | ISO 8601 date (YYYY-MM-DD), required |

**Response 200 OK:**
```json
[
  {
    "timeSlot": "09.00 - 10.00",
    "isAvailable": true,
    "bookedCount": 2,
    "maxCapacity": 5
  },
  {
    "timeSlot": "11.00 - 12.00",
    "isAvailable": false,
    "bookedCount": 5,
    "maxCapacity": 5
  }
]
```

**Default time slots:** `08.00-09.00`, `09.00-10.00`, `10.00-11.00`, `11.00-12.00`, `13.00-14.00`, `14.00-15.00`, `15.00-16.00`  
**Default max capacity per slot:** 5

---

## 4. State Machine

### Consultation Status Values

| Status | Indonesian | Description |
|--------|-----------|-------------|
| `menunggu_konfirmasi` | Waiting for confirmation | Initial state after Warga books |
| `terkonfirmasi` | Confirmed | Balai confirmed the appointment |
| `sedang_berjalan` | In progress | Consultation is actively happening |
| `selesai` | Completed | Consultation is finished |
| `dibatalkan` | Cancelled | Consultation was rejected/cancelled |

### Valid Transitions

```
menunggu_konfirmasi ──→ terkonfirmasi    (Balai confirms)
menunggu_konfirmasi ──→ dibatalkan       (Balai rejects)
terkonfirmasi       ──→ sedang_berjalan  (Auto when time reached)
terkonfirmasi       ──→ dibatalkan       (Balai cancels)
sedang_berjalan     ──→ selesai          (Balai completes)
```

**Terminal states:** `selesai` and `dibatalkan` — no further transitions allowed.

**Implementation:** `ConsultationStatus.canTransitionTo(target)` enforces all transitions. Invalid transitions throw `OC_INVALID_STATE_TRANSITION` (HTTP 409).

---

## 5. Idempotency

### Which Endpoints Require It

All state-changing operations require the `Idempotency-Key` header:

| Endpoint | Method |
|----------|--------|
| `/api/consultations/book` | POST |
| `/api/consultations/{id}/confirm` | PUT |
| `/api/consultations/{id}/reject` | PUT |
| `/api/consultations/{id}/complete` | PUT |

### Flow

1. **Client** sends a request with `Idempotency-Key: <unique-uuid>` header
2. **Controller** calls `IdempotencyService.checkAndCreate(key, userId, endpoint)`
3. If the key is **new** → a `PROCESSING` record is created, controller proceeds
4. If the key is **PROCESSING** → returns 409 with `REQUEST_DUPLICATE`
5. If the key is **SUCCESS** → returns the cached response
6. If the key is **FAILED** → deletes old record, creates new `PROCESSING`, retries
7. **After success** → controller calls `IdempotencyService.markSuccess(key, body, status)`
8. **On exception** → controller calls `IdempotencyService.markFailed(key)`

### Header-Body Validation

For confirm/reject/complete, the request body also contains an `idempotencyKey` field. The controller validates that the header and body values match. Mismatch throws `OC_IDEMPOTENCY_KEY_MISMATCH` (HTTP 400).

### TTL Cleanup

`IdempotencyCleanupScheduler` runs every hour (`@Scheduled(fixedRate = 3_600_000)`) and deletes records whose `ttl` has expired. Default TTL is 24 hours from creation.

---

## 6. Security & Authorization

### Endpoint Access Matrix

| Endpoint | Method | Required Role |
|----------|--------|---------------|
| `/api/balai/list` | GET | **Public** |
| `/api/balai/{id}/available-slots` | GET | **Authenticated** (any role) |
| `/api/consultations/book` | POST | **HOMEOWNER** |
| `/api/consultations/me` | GET | **HOMEOWNER** |
| `/api/consultations/{id}/verify` | POST | **HOMEOWNER** |
| `/api/dashboard/consultations` | GET | **BALAI** |
| `/api/dashboard/stats` | GET | **BALAI** |
| `/api/consultations/{id}/confirm` | PUT | **BALAI** |
| `/api/consultations/{id}/reject` | PUT | **BALAI** |
| `/api/consultations/{id}/complete` | PUT | **BALAI** |

### Authentication

- JWT Bearer token in `Authorization` header
- Token subject contains the user's email
- `JwtAuthenticationFilter` extracts and validates the token
- `@AuthenticationPrincipal UserDetails` in controllers provides the authenticated email

### Role Mapping

| Frontend Role | Backend Role | Spring Authority |
|---------------|-------------|------------------|
| Warga | HOMEOWNER | `ROLE_HOMEOWNER` |
| Balai | BALAI | `ROLE_BALAI` |

---

## 7. Database Schema

### Migration: `038-oc-schema.sql`

#### Table: `balai` (existing — columns added)

| Column | Type | Description |
|--------|------|-------------|
| `location` | VARCHAR(255) | Added — office location |
| `working_hours_start` | VARCHAR(10) | Added — e.g. "07.00" |
| `working_hours_end` | VARCHAR(10) | Added — e.g. "16.00" |

#### Table: `oc_offline_consultations` (new)

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | PK | Auto-generated |
| `user_id` | BIGINT | NOT NULL | FK → `users(id)` |
| `balai_id` | UUID | NOT NULL | FK → `balai(id)` |
| `consultation_date` | DATE | NOT NULL | Appointment date |
| `time_slot` | VARCHAR(30) | NOT NULL | e.g. "09.00 - 10.00" |
| `topic` | VARCHAR(255) | NOT NULL | Consultation topic |
| `description` | TEXT | NULL | Optional details |
| `status` | VARCHAR(30) | NOT NULL | CHECK constraint on valid values |
| `consultation_id` | VARCHAR(50) | NULL | Verification code (set on complete) |
| `cancellation_reason` | TEXT | NULL | Set on reject |
| `notes` | TEXT | NULL | Set on confirm |
| `created_at` | TIMESTAMP | NOT NULL | Auto-set |
| `updated_at` | TIMESTAMP | NOT NULL | Auto-updated |

**Indexes:** `user_id`, `balai_id`, `consultation_date`, `status`

#### Table: `oc_idempotency_store` (new)

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | PK | Auto-generated |
| `idempotency_key` | VARCHAR(255) | NOT NULL, UNIQUE | Client-provided key |
| `user_id` | BIGINT | NULL | Requesting user's ID |
| `endpoint` | VARCHAR(255) | NULL | Endpoint identifier |
| `request_hash` | VARCHAR(255) | NULL | Reserved for future use |
| `response_body` | TEXT | NULL | Cached JSON response (on SUCCESS) |
| `http_status` | INTEGER | NULL | Cached HTTP status code |
| `status` | VARCHAR(20) | NOT NULL | `PROCESSING`, `SUCCESS`, or `FAILED` |
| `created_at` | TIMESTAMP | NOT NULL | Auto-set |
| `ttl` | TIMESTAMP | NOT NULL | Expiration time (24h from creation) |

**Indexes:** `idempotency_key`, `ttl`

---

## 8. Error Codes

| Error Code | HTTP Status | Message | Trigger |
|-----------|-------------|---------|---------|
| `CONSULTATION_NOT_FOUND` | 404 | Consultation not found | Invalid consultation UUID |
| `INVALID_STATE_TRANSITION` | 409 | Invalid consultation state transition | Illegal status change |
| `DUPLICATE_REQUEST` | 409 | This request was already processed | Idempotency key reuse (SUCCESS) |
| `REQUEST_DUPLICATE` | 409 | Same request already being processed | Idempotency key reuse (PROCESSING) |
| `SLOT_UNAVAILABLE` | 409 | Time slot is fully booked | Slot at max capacity |
| `INVALID_VERIFICATION_CODE` | 400 | Verification code is incorrect | Wrong code in verify |
| `IDEMPOTENCY_KEY_MISSING` | 400 | Idempotency-Key header is required | Missing header on state-changing op |
| `IDEMPOTENCY_KEY_MISMATCH` | 400 | Idempotency-Key header must match body | Header ≠ body key |
| `BALAI_NOT_FOUND` | 404 | Balai not found | Invalid balai UUID |
| `USER_NOT_FOUND` | 404 | User not found | JWT email not in database |
| `BAD_REQUEST` | 400 | Various | Invalid UUID, date format, etc. |
| `UNAUTHORIZED` | 401 | Token tidak valid | Missing/invalid JWT |

### Error Response Shape

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable message",
  "statusCode": 400
}
```

Handled by the existing `GlobalDomainExceptionHandler` via `DomainException(ErrorCode)`.

---

## 9. Configuration Files

### Files Created/Modified

| File | Type | Description |
|------|------|-------------|
| `038-oc-schema.sql` | Migration | Schema for consultations + idempotency tables |
| `db.changelog-master.xml` | Modified | Added include for `038-oc-schema.sql` |
| `ErrorCode.java` | Modified | 8 new `OC_*` error codes |
| `SecurityConfig.java` | Modified | Authorization rules for all new endpoints |
| `HubApplication.java` | Modified | Added `@EnableScheduling` |

### Liquibase Changelog Entry

```xml
<include file="db/changelog/038-oc-schema.sql"/>
```

---

## API Contract Alignment Summary

All 10 endpoints specified in the API Contract (`API_CONTRACT.md`) are implemented with matching:

| # | Contract Endpoint | Implementation | Status |
|---|-------------------|----------------|--------|
| 3 | `POST /api/consultations/book` | `WargaConsultationController.bookConsultation()` | ✅ Aligned |
| 4 | `GET /api/consultations/me` | `WargaConsultationController.getMyConsultations()` | ✅ Aligned |
| 5 | `POST /api/consultations/{id}/verify` | `WargaConsultationController.verifyConsultation()` | ✅ Aligned |
| 6 | `GET /api/dashboard/consultations` | `BalaiDashboardController.getDashboardConsultations()` | ✅ Aligned |
| 7 | `PUT /api/consultations/{id}/confirm` | `BalaiDashboardController.confirmConsultation()` | ✅ Aligned |
| 8 | `PUT /api/consultations/{id}/reject` | `BalaiDashboardController.rejectConsultation()` | ✅ Aligned |
| 9 | `PUT /api/consultations/{id}/complete` | `BalaiDashboardController.completeConsultation()` | ✅ Aligned |
| 10 | `GET /api/balai/list` | `BalaiInfoController.getBalaiList()` | ✅ Aligned |
| 11 | `GET /api/balai/{id}/available-slots` | `BalaiInfoController.getAvailableSlots()` | ✅ Aligned |
| 12 | `GET /api/dashboard/stats` | `BalaiDashboardController.getDashboardStats()` | ✅ Aligned |

**Request/Response shapes:** All match the contract exactly.  
**Error codes and HTTP statuses:** All match the contract's error handling specification.  
**Idempotency:** Implemented on all 4 state-changing endpoints as required.  
**State machine:** All 5 valid transitions enforced; terminal states block further changes.  
**Security:** Role-based access matches the contract's authentication requirements.
