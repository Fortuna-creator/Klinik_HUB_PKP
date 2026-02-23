# Offline Consultation API Documentation

## Overview
The Offline Consultation API provides endpoints to manage offline consultation bookings at Balai facilities. This module allows users to schedule, view, update, and cancel offline consultation sessions with training construction facilities (Balai).

## Base URL
```
/api/offline/balai/consultations
```

---

## Endpoints

### 1. Create Consultation Request

**Description:** Creates a new offline consultation booking at a specific Balai facility.

**Endpoint:**
```
POST /api/offline/balai/consultations
```

**Authentication:** Required (User must be authenticated)

**Request Headers:**
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "userId": 1,
  "balaiId": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
  "consultationDate": "2025-11-10",
  "consultationTime": "09:00:00"
}
```

**Field Validations:**
- `userId` (Long, required): The ID of the user booking the consultation
- `balaiId` (String/UUID, required): The ID of the Balai facility
- `consultationDate` (Date, required): Date of the consultation (format: YYYY-MM-DD)
- `consultationTime` (Time, required): Time of the consultation (format: HH:mm:ss)

**Business Rules:**
- Consultation date must be in the future
- Only one consultation per user per time slot at the same Balai
- User and Balai must exist in the system

**Success Response (201 Created):**
```json
{
  "status": "success",
  "message": "Consultation created successfully.",
  "data": {
    "id": "b6c02e8e-22a9-4d50-babc-dc9baf2f1f2a",
    "user": {
      "id": 1,
      "name": "John Doe"
    },
    "balai": {
      "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
      "name": "Balai Pelatihan Konstruksi Jakarta"
    },
    "consultationDate": "2025-11-10",
    "consultationTime": "09:00:00",
    "status": "pending",
    "createdAt": "2025-11-03T10:00:00Z",
    "updatedAt": "2025-11-03T10:00:00Z"
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "status": "error",
  "message": "User ID is required"
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "User or Balai not found."
}
```

**Error Response (409 Conflict):**
```json
{
  "status": "error",
  "message": "Consultation slot already booked for this time."
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": "error",
  "message": "Authentication required."
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "status": "error",
  "message": "Failed to create consultation."
}
```

---

### 2. Get All Consultations

**Description:** Retrieves a list of all consultation bookings in the system.

**Endpoint:**
```
GET /api/offline/balai/consultations
```

**Authentication:** Required (User must be authenticated)

**Request Headers:**
```
Accept: application/json
Authorization: Bearer {token}
```

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "id": "b6c02e8e-22a9-4d50-babc-dc9baf2f1f2a",
      "user": {
        "id": 1,
        "name": "John Doe"
      },
      "balai": {
        "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
        "name": "Balai Pelatihan Konstruksi Jakarta"
      },
      "consultationDate": "2025-11-10",
      "consultationTime": "09:00:00",
      "status": "confirmed",
      "createdAt": "2025-11-03T10:00:00Z",
      "updatedAt": "2025-11-03T11:30:00Z"
    },
    {
      "id": "c7d13f9f-33b0-5e61-cbcd-ed0cbg3g2g3b",
      "user": {
        "id": 2,
        "name": "Jane Smith"
      },
      "balai": {
        "id": "8d6b4d6g-9b88-5e5f-0f1c-52c1c023d6b2",
        "name": "Balai Pelatihan Konstruksi Bandung"
      },
      "consultationDate": "2025-11-12",
      "consultationTime": "14:00:00",
      "status": "pending",
      "createdAt": "2025-11-03T12:15:00Z",
      "updatedAt": "2025-11-03T12:15:00Z"
    }
  ]
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": "error",
  "message": "Authentication required."
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "status": "error",
  "message": "Failed to retrieve consultations."
}
```

---

### 3. Get Consultation by ID

**Description:** Retrieves detailed information about a specific consultation booking.

**Endpoint:**
```
GET /api/offline/balai/consultations/{id}
```

**Authentication:** Required (User must be authenticated)

**Path Parameters:**
- `id` (UUID, required): The unique identifier of the consultation

**Request Headers:**
```
Accept: application/json
Authorization: Bearer {token}
```

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "id": "b6c02e8e-22a9-4d50-babc-dc9baf2f1f2a",
    "user": {
      "id": 1,
      "name": "John Doe"
    },
    "balai": {
      "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
      "name": "Balai Pelatihan Konstruksi Jakarta"
    },
    "consultationDate": "2025-11-10",
    "consultationTime": "09:00:00",
    "status": "confirmed",
    "createdAt": "2025-11-03T10:00:00Z",
    "updatedAt": "2025-11-03T11:30:00Z"
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "Consultation not found."
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": "error",
  "message": "Authentication required."
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "status": "error",
  "message": "Failed to retrieve consultation."
}
```

---

### 4. Update Consultation Status

**Description:** Updates the status of an existing consultation booking.

**Endpoint:**
```
PATCH /api/offline/balai/consultations/{id}
```

**Authentication:** Required (User must be authenticated)

**Path Parameters:**
- `id` (UUID, required): The unique identifier of the consultation to update

**Request Headers:**
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "status": "confirmed"
}
```

**Field Validations:**
- `status` (String, required): Must be one of: `pending`, `confirmed`, `completed`, `cancelled`

**Business Rules:**
- Status transitions should follow logical flow:
  - `pending` → `confirmed` or `cancelled`
  - `confirmed` → `completed` or `cancelled`
  - `completed` and `cancelled` are terminal states

**Success Response (200 OK):**
```json
{
  "status": "success",
  "message": "Consultation status updated.",
  "data": {
    "id": "b6c02e8e-22a9-4d50-babc-dc9baf2f1f2a",
    "user": {
      "id": 1,
      "name": "John Doe"
    },
    "balai": {
      "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
      "name": "Balai Pelatihan Konstruksi Jakarta"
    },
    "consultationDate": "2025-11-10",
    "consultationTime": "09:00:00",
    "status": "confirmed",
    "createdAt": "2025-11-03T10:00:00Z",
    "updatedAt": "2025-11-03T12:00:00Z"
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "status": "error",
  "message": "Status must be one of: pending, confirmed, completed, cancelled"
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "Invalid status or consultation not found."
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": "error",
  "message": "Authentication required."
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "status": "error",
  "message": "Failed to update consultation status."
}
```

---

### 5. Delete Consultation

**Description:** Deletes a consultation booking from the system.

**Endpoint:**
```
DELETE /api/offline/balai/consultations/{id}
```

**Authentication:** Required (User must be authenticated)

**Path Parameters:**
- `id` (UUID, required): The unique identifier of the consultation to delete

**Request Headers:**
```
Accept: application/json
Authorization: Bearer {token}
```

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "status": "success",
  "message": "Consultation deleted successfully."
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "Consultation not found."
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": "error",
  "message": "Authentication required."
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "status": "error",
  "message": "Failed to delete consultation."
}
```

---

## Data Models

### Offline Consultation Response Object
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier for the consultation |
| user | UserSummary | Summary information about the user |
| balai | BalaiSummary | Summary information about the Balai |
| consultationDate | Date | Date of the consultation (YYYY-MM-DD) |
| consultationTime | Time | Time of the consultation (HH:mm:ss) |
| status | String | Current status (pending/confirmed/completed/cancelled) |
| createdAt | Timestamp (ISO 8601) | Creation timestamp in UTC |
| updatedAt | Timestamp (ISO 8601) | Last update timestamp in UTC |

### UserSummary Object
| Field | Type | Description |
|-------|------|-------------|
| id | Long | User's unique identifier |
| name | String | User's full name |

### BalaiSummary Object
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Balai's unique identifier |
| name | String | Balai's name |

### Create Offline Consultation Request
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| userId | Long | Yes | Must reference existing user |
| balaiId | String/UUID | Yes | Must reference existing Balai |
| consultationDate | Date | Yes | Format: YYYY-MM-DD, must be future date |
| consultationTime | Time | Yes | Format: HH:mm:ss |

### Update Consultation Status Request
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| status | String | Yes | Must be: pending, confirmed, completed, or cancelled |

---

## Consultation Status Flow

### Status Definitions
- **pending**: Consultation request has been submitted and awaiting confirmation
- **confirmed**: Consultation has been approved and confirmed by the Balai
- **completed**: Consultation session has been completed
- **cancelled**: Consultation has been cancelled by either party

### Status Transitions
```
pending ──┬──> confirmed ──┬──> completed
          │                │
          └──> cancelled <─┘
```

---

## Business Rules

1. **Date Validation**: Consultation date must be in the future (not past dates)
2. **Time Slot Uniqueness**: A user cannot book multiple consultations at the same Balai for the same date and time
3. **User Verification**: User must exist in the system before creating a consultation
4. **Balai Verification**: Balai must exist in the system before creating a consultation
5. **Status Progression**: Status updates should follow logical progression (e.g., cannot directly go from pending to completed)
6. **Authentication**: All endpoints require valid user authentication
7. **Deletion**: Consultations can be deleted at any status (acts as cancellation)

---

## Error Handling

All API endpoints follow a consistent error response format:

```json
{
  "status": "error",
  "message": "Error description"
}
```

### Common HTTP Status Codes
- `200 OK`: Request succeeded
- `201 Created`: Consultation successfully created
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Authentication required or failed
- `404 Not Found`: Requested resource not found
- `409 Conflict`: Resource conflict (e.g., duplicate booking)
- `500 Internal Server Error`: Server-side error occurred

---

## Authentication

All consultation endpoints require authentication. Include a valid JWT token in the Authorization header:

```
Authorization: Bearer {your_jwt_token}
```

If the token is missing or invalid, the API will return a 401 Unauthorized response.

---

## Notes

- All timestamps are in UTC and follow ISO 8601 format
- UUIDs are used for consultation and Balai identifiers
- User IDs are Long integers
- Date format: YYYY-MM-DD (e.g., 2025-11-10)
- Time format: HH:mm:ss (24-hour format, e.g., 09:00:00)
- The API follows RESTful conventions for resource management
- Status updates use PATCH method for partial resource updates
- All authenticated endpoints validate user permissions

---

## Integration Notes

### Working with Balai API
The Offline Consultation API is tightly integrated with the Offline Balai API. Before creating a consultation:
1. Use the Balai API to retrieve available Balai facilities
2. Select a Balai by its ID
3. Create a consultation referencing that Balai ID

### Working with User System
- User IDs reference the existing user management system
- Ensure users are registered before allowing consultation bookings
- User authentication is required for all consultation operations

---

## Future Enhancements

Potential features for future versions:
- Filtering consultations by date range, status, user, or Balai
- Pagination support for large consultation lists
- Time slot availability checking before booking
- Email/SMS notifications for status changes
- Consultation rescheduling functionality
- Admin-only endpoints for managing all consultations
- Calendar view integration
- Conflict detection for overlapping bookings

