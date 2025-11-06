# Offline Balai API Documentation

## Overview
The Offline Balai API provides endpoints to manage Balai (Training Construction Facilities) data. This module allows users to create, retrieve, update, and delete Balai information for offline consultation scheduling purposes.

## Base URL
```
/api/offline/balai
```

---

## Endpoints

### 1. Get All Balai

**Description:** Retrieves a list of all registered Balai facilities.

**Endpoint:**
```
GET /api/offline/balai
```

**Authentication:** Not required

**Request Headers:**
```
Accept: application/json
```

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
      "name": "Balai Pelatihan Konstruksi Jakarta",
      "description": "Fasilitas pelatihan untuk pekerja konstruksi bersertifikat.",
      "webUrl": "https://bpk-jakarta.go.id",
      "email": "info@bpk-jakarta.go.id",
      "phoneNumber": "021-555-1234",
      "imageUrl": "https://example.com/images/balai1.jpg",
      "address": "Jl. Gatot Subroto No. 12, Jakarta",
      "createdAt": "2025-10-27T10:00:00Z",
      "updatedAt": "2025-10-27T10:00:00Z"
    },
    {
      "id": "8d6b4d6g-9b88-5e5f-0f1c-52c1c023d6b2",
      "name": "Balai Pelatihan Konstruksi Bandung",
      "description": "Pusat pelatihan dan sertifikasi pekerja konstruksi.",
      "webUrl": "https://bpk-bandung.go.id",
      "email": "info@bpk-bandung.go.id",
      "phoneNumber": "022-555-5678",
      "imageUrl": "https://example.com/images/balai2.jpg",
      "address": "Jl. Asia Afrika No. 45, Bandung",
      "createdAt": "2025-10-28T08:30:00Z",
      "updatedAt": "2025-10-28T08:30:00Z"
    }
  ]
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "status": "error",
  "message": "Failed to fetch balai data."
}
```

---

### 2. Get Balai by ID

**Description:** Retrieves detailed information about a specific Balai facility by its ID.

**Endpoint:**
```
GET /api/offline/balai/{id}
```

**Authentication:** Not required

**Path Parameters:**
- `id` (UUID, required): The unique identifier of the Balai

**Request Headers:**
```
Accept: application/json
```

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
    "name": "Balai Pelatihan Konstruksi Jakarta",
    "description": "Fasilitas pelatihan untuk pekerja konstruksi bersertifikat.",
    "webUrl": "https://bpk-jakarta.go.id",
    "email": "info@bpk-jakarta.go.id",
    "phoneNumber": "021-555-1234",
    "imageUrl": "https://example.com/images/balai1.jpg",
    "address": "Jl. Gatot Subroto No. 12, Jakarta",
    "createdAt": "2025-10-27T10:00:00Z",
    "updatedAt": "2025-10-27T10:00:00Z"
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "Balai not found."
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "status": "error",
  "message": "Failed to fetch balai data."
}
```

---

### 3. Create Balai

**Description:** Creates a new Balai facility in the system.

**Endpoint:**
```
POST /api/offline/balai
```

**Authentication:** Not required

**Request Headers:**
```
Content-Type: application/json
Accept: application/json
```

**Request Body:**
```json
{
  "name": "Balai Pelatihan Konstruksi Surabaya",
  "description": "Fasilitas pelatihan konstruksi terpadu di Surabaya.",
  "webUrl": "https://bpk-surabaya.go.id",
  "email": "info@bpk-surabaya.go.id",
  "phoneNumber": "031-555-9876",
  "imageUrl": "https://example.com/images/balai3.jpg",
  "address": "Jl. Raya Darmo No. 88, Surabaya"
}
```

**Field Validations:**
- `name` (string, required): Maximum 100 characters
- `description` (string, optional): Text description
- `webUrl` (string, optional): Maximum 255 characters
- `email` (string, optional): Valid email format, maximum 100 characters
- `phoneNumber` (string, optional): Maximum 20 characters
- `imageUrl` (string, optional): Maximum 255 characters
- `address` (string, optional): Maximum 255 characters

**Success Response (201 Created):**
```json
{
  "status": "success",
  "data": {
    "id": "9e7c5e7h-0c99-6f6g-1g2d-63d2d134e7c3",
    "name": "Balai Pelatihan Konstruksi Surabaya",
    "description": "Fasilitas pelatihan konstruksi terpadu di Surabaya.",
    "webUrl": "https://bpk-surabaya.go.id",
    "email": "info@bpk-surabaya.go.id",
    "phoneNumber": "031-555-9876",
    "imageUrl": "https://example.com/images/balai3.jpg",
    "address": "Jl. Raya Darmo No. 88, Surabaya",
    "createdAt": "2025-11-03T14:20:00Z",
    "updatedAt": "2025-11-03T14:20:00Z"
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "status": "error",
  "message": "Name is required"
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "status": "error",
  "message": "Failed to create balai."
}
```

---

### 4. Update Balai

**Description:** Updates an existing Balai facility's information.

**Endpoint:**
```
PUT /api/offline/balai/{id}
```

**Authentication:** Not required

**Path Parameters:**
- `id` (UUID, required): The unique identifier of the Balai to update

**Request Headers:**
```
Content-Type: application/json
Accept: application/json
```

**Request Body:**
```json
{
  "name": "Balai Pelatihan Konstruksi Jakarta - Updated",
  "description": "Fasilitas pelatihan untuk pekerja konstruksi bersertifikat dan non-bersertifikat.",
  "webUrl": "https://bpk-jakarta-new.go.id",
  "email": "contact@bpk-jakarta.go.id",
  "phoneNumber": "021-555-1234",
  "imageUrl": "https://example.com/images/balai1-updated.jpg",
  "address": "Jl. Gatot Subroto No. 12A, Jakarta"
}
```

**Field Validations:** Same as Create Balai

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
    "name": "Balai Pelatihan Konstruksi Jakarta - Updated",
    "description": "Fasilitas pelatihan untuk pekerja konstruksi bersertifikat dan non-bersertifikat.",
    "webUrl": "https://bpk-jakarta-new.go.id",
    "email": "contact@bpk-jakarta.go.id",
    "phoneNumber": "021-555-1234",
    "imageUrl": "https://example.com/images/balai1-updated.jpg",
    "address": "Jl. Gatot Subroto No. 12A, Jakarta",
    "createdAt": "2025-10-27T10:00:00Z",
    "updatedAt": "2025-11-03T15:30:00Z"
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "Balai not found."
}
```

**Error Response (400 Bad Request):**
```json
{
  "status": "error",
  "message": "Invalid email format"
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "status": "error",
  "message": "Failed to update balai."
}
```

---

### 5. Delete Balai

**Description:** Deletes a Balai facility from the system.

**Endpoint:**
```
DELETE /api/offline/balai/{id}
```

**Authentication:** Not required

**Path Parameters:**
- `id` (UUID, required): The unique identifier of the Balai to delete

**Request Headers:**
```
Accept: application/json
```

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": null
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "Balai not found."
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "status": "error",
  "message": "Failed to delete balai."
}
```

---

## Data Models

### Balai Response Object
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier for the Balai |
| name | String | Name of the Balai facility |
| description | String | Detailed description of the Balai |
| webUrl | String | Official website URL |
| email | String | Contact email address |
| phoneNumber | String | Contact phone number |
| imageUrl | String | URL to the Balai's image |
| address | String | Physical address of the Balai |
| createdAt | Timestamp (ISO 8601) | Creation timestamp in UTC |
| updatedAt | Timestamp (ISO 8601) | Last update timestamp in UTC |

### Balai Request Object
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| name | String | Yes | Max 100 characters |
| description | String | No | Text |
| webUrl | String | No | Max 255 characters |
| email | String | No | Valid email format, max 100 characters |
| phoneNumber | String | No | Max 20 characters |
| imageUrl | String | No | Max 255 characters |
| address | String | No | Max 255 characters |

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
- `201 Created`: Resource successfully created
- `400 Bad Request`: Invalid request data or validation error
- `404 Not Found`: Requested resource not found
- `500 Internal Server Error`: Server-side error occurred

---

## Notes
- All timestamps are in UTC and follow ISO 8601 format
- UUIDs are used for all resource identifiers
- Currently, no authentication is required for Balai endpoints (may change in future versions)
- The API follows RESTful conventions for resource management

