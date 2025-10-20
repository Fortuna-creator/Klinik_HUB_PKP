# Hub API Documentation

## Overview
The Hub API provides comprehensive endpoints for authentication, consultation management, and user services. This documentation covers all available API endpoints with detailed request/response examples and integration guidelines.

## Table of Contents
- [Authentication API](#hub-authentication-api-documentation)
- [Consultation API](#hub-consultation-api-documentation)
- [File Operations API](#hub-file-operations-api-documentation)
- [Project API](#hub-project-api-documentation)
- [User API](#hub-user-api-documentation)

---

# Hub Authentication API Documentation

## Overview
The Hub Authentication API provides endpoints for user registration, OTP verification, login, and profile updates with image upload functionality.

## Base URL
```
http://localhost:8080/api/auth
```

## Authentication
Most endpoints require JWT token authentication. Include the token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

## Endpoints

### 1. Register User
**Endpoint:** `POST /api/auth/register`

**Description:** Registers a new user and sends OTP for verification.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "confirmPassword": "password123"
}
```

**Response (Success - 200):**
```
User registered, OTP sent to user@example.com
```

**Response (Error - 400):**
```
Passwords do not match
```
or
```
Email already exists
```

### 2. Verify OTP
**Endpoint:** `POST /api/auth/verify-otp`

**Description:** Verifies the OTP code sent to the user's email.

**Request Body:**
```json
{
  "userId": 1,
  "otpCode": "123456"
}
```

**Response (Success - 200):**
```
OTP verified successfully
```

**Response (Error - 400):**
```
Invalid OTP
```

### 3. Update Profile
**Endpoint:** `POST /api/auth/profile-update`

**Description:** Updates user profile information with optional KTP and selfie image uploads. Supports both partial and full updates.

**Authentication:** Required (JWT token)

**Content-Type:** `multipart/form-data`

**Request Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | User's full name |
| phone | string | Yes | User's phone number |
| address | string | Yes | User's address |
| ktpNumber | string | Yes | KTP (ID card) number |
| ktpFile | file | No | KTP image (JPEG/PNG, max 5MB) |
| selfieFile | file | No | Selfie image (JPEG/PNG, max 5MB) |

**Example Request (cURL):**
```bash
curl -X POST "http://localhost:8080/api/auth/profile-update" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "name=John Doe" \
  -F "phone=+1234567890" \
  -F "address=123 Main St" \
  -F "ktpNumber=1234567890123456" \
  -F "ktpFile=@ktp_image.jpg" \
  -F "selfieFile=@selfie_image.png"
```

**Response (Success - 200):**
```
Profile updated, awaiting approval
```

**Response (Error - 400):**
```
User not eligible for profile update
```
or
```
Name is required
```

**Response (Error - 401):**
```
Invalid token
```

**Response (Error - 500):**
```
An error occurred
```

**Image Upload Features:**
- **Formats:** JPEG, PNG only
- **Max Size:** 5MB per image
- **Compression:** Automatic resizing to max 1920x1080 pixels for optimization
- **Storage:** Secure storage with unique filenames
- **Validation:** File type and size validation

### 4. Login
**Endpoint:** `POST /api/auth/login`

**Description:** Authenticates user and returns JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (Success - 200):**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (Error - 400):**
```
User not found
```
or
```
Invalid credentials
```
or
```
User not verified
```
or
```
User account is inactive
```

## Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad Request (validation errors, invalid data) |
| 401 | Unauthorized (invalid/missing token) |
| 500 | Internal Server Error |

## Integration Steps

### 1. User Registration Flow
1. Call `POST /api/auth/register` with user email and password
2. System sends OTP to user's email
3. Call `POST /api/auth/verify-otp` with userId and OTP code
4. OTP verification completes registration

### 2. Profile Update Flow
1. User logs in via `POST /api/auth/login` to get JWT token
2. Call `POST /api/auth/profile-update` with Authorization header and form data
3. Images are automatically validated, compressed, and stored
4. Profile status changes to "VERIFICATION_IN_PROCESS"
5. Admin reviews and approves the profile

### 3. Image Upload Guidelines
- Prepare images in JPEG or PNG format
- Ensure file size is under 5MB
- Images larger than 1920x1080 will be automatically resized
- Use multipart/form-data for file uploads
- Images are optional - can update profile without images

### 4. Error Handling
- Always check response status codes
- Handle 401 errors by re-authenticating
- Validate input data before sending requests
- Implement retry logic for 500 errors

## Security Considerations
- JWT tokens expire after 1 hour (configurable)
- Image files are stored securely with unique filenames
- User authentication required for profile updates
- Passwords are hashed using BCrypt
- File uploads are validated for type and size

## Postman Collection
Import `Hub_Auth_Postman_Collection.json` for testing all endpoints with pre-configured requests.

---

# Hub Consultation API Documentation

## Overview
The Hub Consultation API provides endpoints for managing consultation requests between home owners and consultants, including consultation creation, status management, and lifecycle operations.

## Base URL
```
http://localhost:8080/api/consultations
```

## Authentication
All endpoints require JWT token authentication. Include the token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

## Endpoints

### 1. Create Consultation
**Endpoint:** `POST /api/consultations`

**Description:** Creates a new consultation request from a home owner to a consultant.

**Authorization:** Required (Home Owner role)

**Request Body:**
```json
{
  "consultantId": 1,
  "consultationType": "GRATIS",
  "channel": "CHAT"
}
```

**Request Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| consultantId | number | Yes | ID of the consultant to request consultation from |
| consultationType | string | Yes | Type of consultation: `GRATIS` or `BERBAYAR` |
| channel | string | Yes | Communication channel: `CHAT`, `VIDEO`, or `WHATSAPP` |

**Response (Success - 200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "homeOwnerId": 2,
  "homeOwnerName": "John Doe",
  "consultantId": 1,
  "consultantName": "Jane Smith",
  "designId": null,
  "designName": null,
  "consultationType": "GRATIS",
  "channel": "CHAT",
  "status": "MENUNGGU_KONFIRMASI_KONSULTAN",
  "startedAt": null,
  "completedAt": null,
  "createdAt": "2025-10-20T09:50:00Z",
  "updatedAt": "2025-10-20T09:50:00Z"
}
```

**Response (Error - 500):**
```json
{
  "errorCode": "INTERNAL_ERROR",
  "message": "An error occurred during consultation creation"
}
```

### 2. Get Consultations
**Endpoint:** `GET /api/consultations`

**Description:** Retrieves all consultations for the authenticated user (filtered by role).

**Authorization:** Required (Home Owner or Consultant role)

**Response (Success - 200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "homeOwnerId": 2,
    "homeOwnerName": "John Doe",
    "consultantId": 1,
    "consultantName": "Jane Smith",
    "designId": "660e8400-e29b-41d4-a716-446655440001",
    "designName": "Modern House Design",
    "consultationType": "GRATIS",
    "channel": "CHAT",
    "status": "MENUNGGU_JADWAL_SURVEY",
    "startedAt": null,
    "completedAt": null,
    "createdAt": "2025-10-20T09:50:00Z",
    "updatedAt": "2025-10-20T09:50:00Z"
  }
]
```

### 3. Get Consultation by ID
**Endpoint:** `GET /api/consultations/{id}`

**Description:** Retrieves a specific consultation by ID.

**Authorization:** Required (Home Owner or Consultant role)

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | UUID of the consultation |

**Response (Success - 200):** Same as create consultation response

**Response (Error - 500):**
```json
{
  "errorCode": "INTERNAL_ERROR",
  "message": "Consultation not found or access denied"
}
```

### 4. Accept Consultation
**Endpoint:** `POST /api/consultations/{id}/accept`

**Description:** Consultant accepts a consultation request, changing status to waiting for survey schedule.

**Authorization:** Required (Consultant role)

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | UUID of the consultation |

**Response (Success - 200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "MENUNGGU_JADWAL_SURVEY",
  "homeOwnerId": 2,
  "homeOwnerName": "John Doe",
  "consultantId": 1,
  "consultantName": "Jane Smith",
  "consultationType": "GRATIS",
  "channel": "CHAT",
  "createdAt": "2025-10-20T09:50:00Z",
  "updatedAt": "2025-10-20T09:50:00Z"
}
```

**Response (Error - 500):**
```json
{
  "errorCode": "INTERNAL_ERROR",
  "message": "Invalid status transition"
}
```

### 5. Reject Consultation
**Endpoint:** `POST /api/consultations/{id}/reject`

**Description:** Consultant rejects a consultation request, marking it as completed (rejected).

**Authorization:** Required (Consultant role)

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | UUID of the consultation |

**Response (Success - 200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SELESAI",
  "homeOwnerId": 2,
  "homeOwnerName": "John Doe",
  "consultantId": 1,
  "consultantName": "Jane Smith",
  "consultationType": "GRATIS",
  "channel": "CHAT",
  "createdAt": "2025-10-20T09:50:00Z",
  "updatedAt": "2025-10-20T09:50:00Z"
}
```

### 6. Finalize Consultation
**Endpoint:** `PATCH /api/consultations/{id}/finalize`

**Description:** Home owner finalizes a completed consultation.

**Authorization:** Required (Home Owner role)

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | UUID of the consultation |

**Response (Success - 200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "FINAL",
  "homeOwnerId": 2,
  "homeOwnerName": "John Doe",
  "consultantId": 1,
  "consultantName": "Jane Smith",
  "consultationType": "GRATIS",
  "channel": "CHAT",
  "createdAt": "2025-10-20T09:50:00Z",
  "updatedAt": "2025-10-20T09:50:00Z"
}
```

## Consultation Status Flow

```
MENUNGGU_KONFIRMASI_KONSULTAN (Waiting for consultant confirmation)
    ├── Accept → MENUNGGU_JADWAL_SURVEY (Waiting for survey schedule)
    └── Reject → SELESAI (Completed/Rejected)

MENUNGGU_JADWAL_SURVEY
    └── Schedule survey → MENUNGGU_APPROVAL_JADWAL (Waiting for schedule approval)

MENUNGGU_APPROVAL_JADWAL
    ├── Approve → SURVEY_DIJADWALKAN (Survey scheduled)
    └── Propose new schedule → MENUNGGU_APPROVAL_JADWAL (loop)

SURVEY_DIJADWALKAN
    └── Survey completed → SURVEY_SELESAI (Survey completed)

SURVEY_SELESAI
    └── Start consultation → AKTIF (Active consultation)

AKTIF
    ├── Enter revision → REVISI (Revision phase)
    └── Complete → FINAL (Final)

REVISI
    └── Complete revision → FINAL

FINAL
    └── Mark as done → SELESAI
```

## Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad Request (validation errors) |
| 401 | Unauthorized (invalid/missing token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found |
| 500 | Internal Server Error |

## Integration Steps

### 1. Consultation Creation Flow
1. Home owner calls `POST /api/consultations` with consultant ID, type, and channel
2. System creates consultation with status `MENUNGGU_KONFIRMASI_KONSULTAN`
3. Consultant receives notification (implementation depends on notification system)

### 2. Consultation Acceptance Flow
1. Consultant calls `POST /api/consultations/{id}/accept` to accept
2. Status changes to `MENUNGGU_JADWAL_SURVEY`
3. System can proceed with survey scheduling workflow

### 3. Consultation Rejection Flow
1. Consultant calls `POST /api/consultations/{id}/reject` to reject
2. Status changes to `SELESAI` (consultation ends)

### 4. Consultation Finalization Flow
1. Home owner calls `PATCH /api/consultations/{id}/finalize` when satisfied
2. Status changes to `FINAL`

## Error Handling
- Always check HTTP status codes
- Handle 401 errors by re-authenticating
- Validate consultation IDs are valid UUIDs
- Check user permissions for role-based endpoints
- Implement retry logic for 500 errors

## Security Considerations
- JWT tokens required for all endpoints
- Role-based access control (HOME_OWNER vs CONSULTANT)
- Consultation ownership validation
- Status transition validation to prevent invalid state changes

## Postman Collection
Import `Hub_API_v2.1_Postman_Collection.json` for testing consultation endpoints with pre-configured requests.