# HUB Backend API Documentation

## Health Check API

### Overview
The Health Check API provides a simple endpoint to verify that the application is running.

### Endpoints

#### 1. Health Check
**GET** `/health`

Returns the health status of the application.

**Request Example:**
```bash
curl -X GET "http://localhost:8080/health"
```

**Response (200 OK):**
```json
{
  "status": "UP"
}
```

## Authentication API

### Overview
The Authentication API handles user registration, login, OTP verification, and account activation.

### Authentication
Most endpoints do not require authentication, except where noted.

### Endpoints

#### 1. Register User
**POST** `/api/auth/register`

Register a new user account with email and password.

**Parameters:**
- `email` (string, required): User's email address
- `password` (string, required): User's password
- `confirmPassword` (string, required): Password confirmation

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "confirmPassword": "password123"
  }'
```

**Response (200 OK):**
```json
{
  "role": "HOMEOWNER",
  "email": "user@example.com",
  "phone": null,
  "status": "PENDING_VERIFICATION",
  "userId": 1,
  "fullName": null
}
```

#### 2. Verify OTP
**POST** `/api/auth/verify-otp`

Verify the OTP sent to the user's email for account verification.

**Parameters:**
- `email` (string, required): User's email address
- `otp` (string, required): One-time password

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/verify-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "otp": "123456"
  }'
```

**Response (200 OK):**
```json
{
  "token": "jwt_token_here",
  "refreshToken": "refresh_token_here",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "role": "HOMEOWNER"
  }
}
```

#### 3. Login
**POST** `/api/auth/login`

Authenticate user with email and password.

**Parameters:**
- `email` (string, required): User's email address
- `password` (string, required): User's password

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Response (200 OK):**
```json
{
  "token": "jwt_token_here",
  "refreshToken": "refresh_token_here",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "role": "HOMEOWNER"
  }
}
```

#### 4. Activate Account
**POST** `/api/auth/activate-account`

Activate user account after OTP verification.

**Parameters:**
- `email` (string, required): User's email address
- `password` (string, required): User's password
- `fullName` (string, required): User's full name
- `phone` (string, required): User's phone number
- `role` (string, required): User role ("HOMEOWNER" or "CONSULTANT")

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/activate-account" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "phone": "+1234567890",
    "role": "HOMEOWNER"
  }'
```

**Response (200 OK):**
```json
"Account activated successfully for user@example.com"
```

#### 5. Resend Activation Email
**POST** `/api/auth/resend-activation`

Resend activation email with new OTP.

**Parameters:**
- `email` (query, required): User's email address

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/resend-activation?email=user@example.com"
```

**Response (200 OK):**
```json
"Activation email resent successfully"
```

### Error Responses

**400 Bad Request:**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Passwords do not match"
}
```

**401 Unauthorized:**
```json
{
  "errorCode": "INVALID_CREDENTIALS",
  "message": "Invalid email or password"
}
```

**404 Not Found:**
```json
{
  "errorCode": "USER_NOT_FOUND",
  "message": "User not found"
}
```

## Backoffice API

### Overview
The Backoffice API provides administrative functions for managing users and consultants.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header and admin role.

### Endpoints

#### 1. Get Users for Verification
**GET** `/api/balai/verification-pending`

Get list of users pending verification.

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/balai/verification-pending" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "status": "PENDING_VERIFICATION"
  }
]
```

#### 2. Approve User
**POST** `/api/balai/approve/{userId}`

Approve a user account.

**Parameters:**
- `userId` (path): User ID to approve

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/balai/approve/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 3. Reject User
**POST** `/api/balai/reject/{userId}`

Reject a user account.

**Parameters:**
- `userId` (path): User ID to reject

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/balai/reject/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 4. Deactivate User
**POST** `/api/balai/deactivate/{userId}`

Deactivate a user account.

**Parameters:**
- `userId` (path): User ID to deactivate

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/balai/deactivate/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 5. Register Consultant
**POST** `/api/balai/register-consultant`

Register a new consultant (admin function).

**Parameters:**
- `email` (string, required): Consultant's email
- `password` (string, required): Consultant's password
- `fullName` (string, required): Consultant's full name
- `phone` (string, required): Consultant's phone
- `consultantType` (string, required): "PROFESSIONAL" or "STUDENT"
- `specialty` (string, optional): Consultant's specialty
- `experienceYears` (integer, optional): Years of experience
- `certifications` (string, optional): Certifications
- `portfolioUrl` (string, optional): Portfolio URL

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/balai/register-consultant" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "email": "consultant@example.com",
    "password": "password123",
    "fullName": "Jane Consultant",
    "phone": "+1234567890",
    "consultantType": "PROFESSIONAL",
    "specialty": "Architecture",
    "experienceYears": 5
  }'
```

#### 6. Bulk Register Consultants
**POST** `/api/balai/register-consultant/bulk`

Bulk register consultants from CSV file.

**Parameters:**
- `file` (multipart/form-data, required): CSV file with consultant data

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/balai/register-consultant/bulk" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "file=@consultants.csv"
```

## File Management API

### Overview
The File Management API handles file uploads and downloads using MinIO storage.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Upload File
**POST** `/api/files/upload`

Upload a file to MinIO storage.

**Parameters:**
- `file` (multipart/form-data, required): File to upload
- `fileType` (string, required): File type ("KTP", "Document", or "Selfie")

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/files/upload" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "file=@document.pdf" \
  -F "fileType=Document"
```

**Response (200 OK):**
```json
{
  "fileId": "documents/document.pdf",
  "fileUrl": "/hub-files/documents/document.pdf?checksum=abc123",
  "checksum": "abc123"
}
```

#### 2. Download File
**GET** `/api/files/download/{fileId}`

Download a file from MinIO storage.

**Parameters:**
- `fileId` (path): File ID to download

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/files/download/documents/document.pdf" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  --output document.pdf
```

## Project Management API

### Overview
The Project Management API allows users to create and manage their projects. Projects can have different statuses: PENDING (waiting for consultation acceptance), ACTIVE (consultation accepted), COMPLETED (project finished), or CANCELLED (project cancelled).

### Project Status Flow
1. **PENDING**: Project created and consultation requested, waiting for consultant acceptance
2. **ACTIVE**: Consultation accepted by consultant, project is active
3. **COMPLETED**: Project finished successfully
4. **CANCELLED**: Project cancelled

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Project Status Flow
1. **PENDING**: Project created and consultation requested, waiting for consultant acceptance
2. **ACTIVE**: Consultation accepted by consultant, project is active
3. **COMPLETED**: Project finished successfully
4. **CANCELLED**: Project cancelled

### Endpoints

#### 1. Create Project
**POST** `/api/projects`

Create a new project. New projects are automatically assigned ACTIVE status.

**Parameters:**
- `name` (string, required): Project name
- `longitude` (number, required): Project longitude coordinate
- `latitude` (number, required): Project latitude coordinate
- `locationDetail` (string, required): Detailed project location address
- `type` (string, required): Project type ("PROTOTYPE" or "NON_PROTOTYPE")
- `landArea` (number, required): Land area in square meters
- `income` (number, required): Project income/budget

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/projects" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rumah Minimalis",
    "longitude": 106.8456,
    "latitude": -6.2088,
    "locationDetail": "Jl. Sudirman No. 123, Jakarta Selatan",
    "type": "PROTOTYPE",
    "landArea": 200.5,
    "income": 500000000
  }'
```

**Response (201 Created):**
```json
{
  "projectId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Rumah Minimalis",
  "type": "PROTOTYPE",
  "createdAt": "2024-01-15T10:00:00Z"
}
```

#### 2. Get User Projects
**GET** `/api/projects`

Get list of user's projects with optional filters. Supports filtering by status to show PENDING projects.

**Parameters:**
- `page` (query, optional): Page number (0-based, default: 0)
- `size` (query, optional): Page size (default: 10)
- `type` (query, optional): Filter by project type ("PROTOTYPE" or "NON_PROTOTYPE")
- `status` (query, optional): Filter by project status ("PENDING", "ACTIVE", "COMPLETED", "CANCELLED")

**Note:** Use `status=PENDING` to view projects that are waiting for consultation acceptance.

**Request Example (Get all projects):**
```bash
curl -X GET "http://localhost:8080/api/projects" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Request Example (Get PENDING projects only - projects waiting for consultation acceptance):**
```bash
curl -X GET "http://localhost:8080/api/projects?status=PENDING" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "projects": [
    {
      "projectId": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Rumah Minimalis",
      "type": "PROTOTYPE",
      "status": "PENDING",
      "location": {
        "longitude": 106.8456,
        "latitude": -6.2088,
        "address": "Jl. Sudirman No. 123, Jakarta Selatan"
      },
      "landArea": 200.5,
      "createdAt": "2024-01-15T10:00:00Z"
    }
  ]
}
```

#### 3. Get Project Detail
**GET** `/api/projects/{id}`

Get detailed information about a specific project.

**Parameters:**
- `id` (path): Project UUID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/projects/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "projectId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Rumah Minimalis",
  "type": "PROTOTYPE",
  "status": "PENDING",
  "locationDetail": "Jl. Sudirman No. 123, Jakarta Selatan",
  "longitude": 106.8456,
  "latitude": -6.2088,
  "landArea": 200.5,
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z",
  "consultation": null,
  "permit": null
}
```

## User Management API

### Overview
The User Management API provides endpoints for user profile management, consultant search, and portfolio management.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Get All Users
**GET** `/api/user`

Get list of all users (admin only).

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/user" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 2. Activate User
**POST** `/api/user/{id}/activate`

Activate a user account (admin only).

**Parameters:**
- `id` (path): User ID

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/user/1/activate" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 3. Deactivate User
**POST** `/api/user/{id}/deactivate`

Deactivate a user account (admin only).

**Parameters:**
- `id` (path): User ID

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/user/1/deactivate" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 4. Self Deactivate User
**POST** `/api/user/{id}/self-deactivate`

Allow user to deactivate their own account.

**Parameters:**
- `id` (path): User ID (must match authenticated user)

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/user/1/self-deactivate" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 5. Get Consultant Portfolios
**GET** `/api/user/portfolio`

Get portfolios for authenticated consultant.

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/user/portfolio" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 6. Get First Portfolio
**GET** `/api/user/portfolio/first`

Get first portfolio for authenticated consultant.

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/user/portfolio/first" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 7. Get Consultant Portfolio by ID
**GET** `/api/user/consultants/{consultantId}/portfolio`

Get portfolio for a specific consultant (for homeowners).

**Parameters:**
- `consultantId` (path): Consultant ID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/user/consultants/1/portfolio" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 8. Create/Update Portfolio
**POST/PUT** `/api/user/portfolio`

Create or update consultant portfolio.

**Parameters:**
- `title` (string, required): Portfolio title
- `description` (string, required): Portfolio description
- `experienceYears` (integer, required): Years of experience
- `specialty` (string, required): Consultant specialty
- `certifications` (string, optional): Certifications
- `portfolioUrl` (string, optional): Portfolio website URL
- `consultantType` (string, required): "PROFESSIONAL" or "STUDENT"
- `location` (object, required): Location coordinates
  - `latitude` (number): Latitude
  - `longitude` (number): Longitude

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/user/portfolio" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Architectural Design Services",
    "description": "Professional architectural services",
    "experienceYears": 5,
    "specialty": "Architecture",
    "certifications": "Licensed Architect",
    "consultantType": "PROFESSIONAL",
    "location": {
      "latitude": -6.2088,
      "longitude": 106.8456
    }
  }'
```

#### 9. Get Completed Portfolios
**GET** `/api/user/consultants/completed`

Get list of completed portfolios.

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/user/consultants/completed" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 10. Search Consultants
**GET** `/api/user/consultants`

Search consultants near a location with filters.

**Parameters:**
- `lat` (query, required): Latitude
- `long` (query, required): Longitude
- `page` (query, optional): Page number (default: 0)
- `size` (query, optional): Page size (default: 10)
- `type` (query, optional): Consultant type ("PROFESSIONAL" or "STUDENT")
- `specialty` (query, optional): Specialty filter
- `sortBy` (query, optional): Sort criteria

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/user/consultants?lat=-6.2088&long=106.8456&page=0&size=10&type=PROFESSIONAL" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 11. Update Profile
**POST** `/api/user/verification`

Update homeowner profile information.

**Parameters:**
- `fullName` (string, required): Full name
- `phone` (string, required): Phone number
- `address` (string, optional): Address
- `location` (object, optional): Location coordinates
  - `latitude` (number): Latitude
  - `longitude` (number): Longitude

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/user/verification" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "phone": "+1234567890",
    "address": "123 Main St",
    "location": {
      "latitude": -6.2088,
      "longitude": 106.8456
    }
  }'
```

#### 12. Add Portfolio Project
**POST** `/api/user/portfolio/projects`

Add a project to consultant's portfolio.

**Parameters:**
- `title` (string, required): Project title
- `description` (string, required): Project description
- `location` (string, required): Project location
- `completionDate` (string, required): Completion date (ISO format)
- `projectType` (string, required): Project type
- `budget` (number, optional): Project budget
- `imageUrls` (array, optional): Array of image URLs

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/user/portfolio/projects" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Modern Kitchen Renovation",
    "description": "Complete kitchen renovation with modern appliances",
    "location": "Jakarta",
    "completionDate": "2023-12-01",
    "projectType": "RENOVATION",
    "budget": 25000,
    "imageUrls": ["/hub-files/portfolio/image1.jpg"]
  }'
```

#### 13. Upload Portfolio Images
**POST** `/api/user/portfolio/upload-images`

Upload images for portfolio.

**Parameters:**
- `files` (multipart/form-data, required): Array of image files

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/user/portfolio/upload-images" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "files=@image1.jpg" \
  -F "files=@image2.jpg"
```

## Consultation Management API

### Overview
The Consultation Management API handles the lifecycle of consultations between homeowners and consultants.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Create Consultation
**POST** `/api/consultations`

Create a new consultation request (homeowner only). When a consultation is created, the associated project status automatically changes to PENDING.

**Parameters:**
- `consultantId` (Long, required): Target consultant ID
- `projectId` (UUID, required): Associated project ID (project status will automatically change to PENDING)
- `consultationType` (string, required): "ONLINE" or "OFFLINE"
- `channel` (string, required): "CHAT" or "VIDEO_CALL"

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/consultations" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "consultantId": 1,
    "projectId": "123e4567-e89b-12d3-a456-426614174000",
    "consultationType": "ONLINE",
    "channel": "CHAT"
  }'
```

**Response (200 OK):**
```json
{
  "id": "456e7890-e89b-12d3-a456-426614174001",
  "homeOwnerId": 1,
  "homeOwnerName": "John Doe",
  "consultantId": 2,
  "consultantName": "Jane Consultant",
  "designId": null,
  "designName": null,
  "consultationType": "ONLINE",
  "channel": "CHAT",
  "status": "MENUNGGU_KONFIRMASI_KONSULTAN",
  "startedAt": null,
  "completedAt": null,
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

**Note:** After this request, the associated project status will automatically change from ACTIVE to PENDING, indicating it's waiting for consultant acceptance. Use `GET /api/projects?status=PENDING` to view projects in this state.

#### 2. Get Consultations
**GET** `/api/consultations`

Get list of user's consultations.

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/consultations" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 3. Get Consultation Detail
**GET** `/api/consultations/{id}`

Get detailed information about a consultation.

**Parameters:**
- `id` (path): Consultation UUID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/consultations/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 4. Accept Consultation
**POST** `/api/consultations/{id}/accept`

Accept a consultation request (consultant only). When a consultation is accepted, the associated project status automatically changes to ACTIVE.

**Parameters:**
- `id` (path): Consultation UUID

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/consultations/123e4567-e89b-12d3-a456-426614174000/accept" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "homeOwnerId": 1,
  "homeOwnerName": "John Doe",
  "consultantId": 2,
  "consultantName": "Jane Consultant",
  "designId": null,
  "designName": null,
  "consultationType": "ONLINE",
  "channel": "CHAT",
  "status": "MENUNGGU_JADWAL_SURVEY",
  "startedAt": null,
  "completedAt": null,
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Note:** After this request, the associated project status will automatically change from PENDING to ACTIVE, indicating the consultation is now active and the project can proceed.

#### 5. Reject Consultation
**POST** `/api/consultations/{id}/reject`

Reject a consultation request (consultant only).

**Parameters:**
- `id` (path): Consultation UUID

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/consultations/123e4567-e89b-12d3-a456-426614174000/reject" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 6. Finalize Consultation
**PATCH** `/api/consultations/{id}/finalize`

Mark consultation as finalized (homeowner only).

**Parameters:**
- `id` (path): Consultation UUID

**Request Example:**
```bash
curl -X PATCH "http://localhost:8080/api/consultations/123e4567-e89b-12d3-a456-426614174000/finalize" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Contract Management API

### Overview
The Contract Management API handles contract creation, viewing, and signing.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Get Active Contract
**GET** `/api/contracts/{consultationId}`

Get the active contract for a consultation.

**Parameters:**
- `consultationId` (path): Consultation UUID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/contracts/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 2. Sign Contract
**PATCH** `/api/contracts/{id}/sign`

Sign a contract.

**Parameters:**
- `id` (path): Contract UUID

**Request Example:**
```bash
curl -X PATCH "http://localhost:8080/api/contracts/123e4567-e89b-12d3-a456-426614174000/sign" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Design Management API

### Overview
The Design Management API provides access to prototype designs and design-related functionality.

### Authentication
Most endpoints are public, but some require authentication.

### Endpoints

#### 1. Get Prototype Designs
**GET** `/api/consultations/designs`

Get list of available prototype designs.

**Parameters:**
- `type` (query, optional): Filter by design type

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/consultations/designs?type=MODERN"
```

#### 2. Get Design Detail
**GET** `/api/consultations/designs/{id}`

Get detailed information about a specific design.

**Parameters:**
- `id` (path): Design UUID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/consultations/designs/123e4567-e89b-12d3-a456-426614174000"
```

#### 3. Download Design Document
**GET** `/api/consultations/designs/{id}/download/{type}`

Download design documents.

**Parameters:**
- `id` (path): Design UUID
- `type` (path): Document type

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/consultations/designs/123e4567-e89b-12d3-a456-426614174000/download/pdf"
```

#### 4. Get SIMBG Link
**GET** `/api/consultations/designs/{id}/simbg`

Get SIMBG integration link for design.

**Parameters:**
- `id` (path): Design UUID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/consultations/designs/123e4567-e89b-12d3-a456-426614174000/simbg"
```

## Favorite Design API

### Overview
The Favorite Design API allows homeowners to favorite and manage their preferred designs.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Toggle Favorite
**POST** `/api/consultations/favorites`

Add or remove a design from favorites.

**Parameters:**
- `designId` (UUID, required): Design ID to favorite/unfavorite

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/consultations/favorites" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "designId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

#### 2. Get Favorites
**GET** `/api/consultations/favorites`

Get list of user's favorite designs.

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/consultations/favorites" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Final Document API

### Overview
The Final Document API handles the upload and management of final project documents.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Upload Final Document
**POST** `/api/final-documents`

Upload final documents for a consultation (consultant only).

**Parameters:**
- `consultationId` (query, required): Consultation UUID
- `fileDedUrl` (query, required): DED file URL
- `fileRabUrl` (query, required): RAB file URL
- `fileSpektekUrl` (query, required): SPEKTEK file URL

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/final-documents?consultationId=123e4567-e89b-12d3-a456-426614174000&fileDedUrl=/files/ded.pdf&fileRabUrl=/files/rab.pdf&fileSpektekUrl=/files/spektek.pdf" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 2. Download Final Document
**GET** `/api/final-documents/{id}/download`

Download final documents (homeowner only).

**Parameters:**
- `id` (path): Final document UUID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/final-documents/123e4567-e89b-12d3-a456-426614174000/download" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 3. Archive Final Document
**PATCH** `/api/final-documents/{id}/archive`

Archive final documents (consultant only).

**Parameters:**
- `id` (path): Final document UUID

**Request Example:**
```bash
curl -X PATCH "http://localhost:8080/api/final-documents/123e4567-e89b-12d3-a456-426614174000/archive" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Payment API

### Overview
The Payment API handles payment creation and management for consultations.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Create Payment
**POST** `/api/payments`

Create a new payment for consultation (homeowner only).

**Parameters:**
- `consultationId` (query, required): Consultation UUID
- `amount` (number, required): Payment amount
- `description` (string, required): Payment description
- `paymentMethod` (string, required): Payment method

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/payments?consultationId=123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000000,
    "description": "Consultation fee",
    "paymentMethod": "BANK_TRANSFER"
  }'
```

#### 2. Get Payments
**GET** `/api/payments`

Get payments for a consultation.

**Parameters:**
- `consultationId` (query, required): Consultation UUID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/payments?consultationId=123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Revision API

### Overview
The Revision API handles revision requests and management.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Create Revision
**POST** `/api/revisions`

Create a revision request (homeowner only).

**Parameters:**
- `consultationId` (query, required): Consultation UUID
- `title` (string, required): Revision title
- `description` (string, required): Revision description
- `priority` (string, required): Revision priority

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/revisions?consultationId=123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Change kitchen layout",
    "description": "Need to modify the kitchen island position",
    "priority": "HIGH"
  }'
```

#### 2. Get Revisions
**GET** `/api/revisions`

Get revisions for a consultation.

**Parameters:**
- `consultationId` (query, required): Consultation UUID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/revisions?consultationId=123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Survey Schedule API

### Overview
The Survey Schedule API manages survey scheduling for consultations.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Create Survey Schedule
**POST** `/api/survey-schedules/consultations/{consultationId}`

Create survey schedule (consultant only).

**Parameters:**
- `consultationId` (path): Consultation UUID
- `scheduledDate` (string, required): Scheduled date (ISO format)
- `scheduledTime` (string, required): Scheduled time
- `location` (string, required): Survey location
- `notes` (string, optional): Additional notes

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/survey-schedules/consultations/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "scheduledDate": "2024-01-15",
    "scheduledTime": "10:00",
    "location": "Jakarta Selatan",
    "notes": "Bring measuring tools"
  }'
```

#### 2. Approve Survey Schedule
**POST** `/api/survey-schedules/consultations/{consultationId}/approve`

Approve survey schedule (homeowner only).

**Parameters:**
- `consultationId` (path): Consultation UUID

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/survey-schedules/consultations/123e4567-e89b-12d3-a456-426614174000/approve" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 3. Reject Survey Schedule
**POST** `/api/survey-schedules/consultations/{consultationId}/reject`

Reject survey schedule (homeowner only).

**Parameters:**
- `consultationId` (path): Consultation UUID

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/survey-schedules/consultations/123e4567-e89b-12d3-a456-426614174000/reject" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 4. Reschedule Survey
**POST** `/api/survey-schedules/consultations/{consultationId}/reschedule`

Propose new survey schedule (homeowner only).

**Parameters:**
- `consultationId` (path): Consultation UUID
- `scheduledDate` (string, required): New scheduled date
- `scheduledTime` (string, required): New scheduled time
- `location` (string, required): New location
- `notes` (string, optional): New notes

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/survey-schedules/consultations/123e4567-e89b-12d3-a456-426614174000/reschedule" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "scheduledDate": "2024-01-20",
    "scheduledTime": "14:00",
    "location": "Jakarta Pusat",
    "notes": "New schedule request"
  }'
```

#### 5. Complete Survey
**POST** `/api/survey-schedules/consultations/{consultationId}/complete`

Mark survey as completed (homeowner only).

**Parameters:**
- `consultationId` (path): Consultation UUID

**Request Example:**
```bash
curl -X POST "http://localhost:8080/api/survey-schedules/consultations/123e4567-e89b-12d3-a456-426614174000/complete" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 6. Get Survey Schedules
**GET** `/api/survey-schedules/consultations/{consultationId}`

Get survey schedules for consultation.

**Parameters:**
- `consultationId` (path): Consultation UUID

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/survey-schedules/consultations/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Chat Messages API

### Overview
The Chat Messages API allows Home Owners and Consultants to exchange text messages and voice notes during approved consultations.

### Authentication
All endpoints require JWT authentication with `Authorization: Bearer <token>` header.

### Endpoints

#### 1. Send Message
**POST** `/api/v1/chats/{consultationId}/messages`

Send a text message or voice note in a consultation chat.

**Parameters:**
- `consultationId` (path): UUID of the consultation
- `messageType` (form): "TEXT" or "VOICE"
- `messageContent` (form, optional): Text content for TEXT messages
- `voiceFile` (form, optional): Multipart file for VOICE messages (MP3/M4A only, max 10MB)

**Request Example (Text Message):**
```bash
curl -X POST "http://localhost:8080/api/v1/chats/123e4567-e89b-12d3-a456-426614174000/messages" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "messageType=TEXT" \
  -F "messageContent=Hello, I have a question about the design"
```

**Request Example (Voice Message):**
```bash
curl -X POST "http://localhost:8080/api/v1/chats/123e4567-e89b-12d3-a456-426614174000/messages" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "messageType=VOICE" \
  -F "voiceFile=@voice_note.mp3"
```

**Response (200 OK):**
```json
{
  "id": "456e7890-e89b-12d3-a456-426614174001",
  "consultationId": "123e4567-e89b-12d3-a456-426614174000",
  "senderId": 1,
  "receiverId": 2,
  "messageType": "TEXT",
  "messageContent": "Hello, I have a question about the design",
  "fileUrl": null,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### 2. Get Chat Messages
**GET** `/api/v1/chats/{consultationId}/messages`

Retrieve all messages in a consultation chat, ordered by creation time.

**Parameters:**
- `consultationId` (path): UUID of the consultation

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/chats/123e4567-e89b-12d3-a456-426614174000/messages" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
[
  {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "consultationId": "123e4567-e89b-12d3-a456-426614174000",
    "senderId": 1,
    "receiverId": 2,
    "messageType": "TEXT",
    "messageContent": "Hello, I have a question about the design",
    "fileUrl": null,
    "createdAt": "2024-01-15T10:30:00Z"
  },
  {
    "id": "567e8901-e89b-12d3-a456-426614174002",
    "consultationId": "123e4567-e89b-12d3-a456-426614174000",
    "senderId": 2,
    "receiverId": 1,
    "messageType": "VOICE",
    "messageContent": null,
    "fileUrl": "/hub-files/chat/voice/voice_note.mp3",
    "createdAt": "2024-01-15T10:31:00Z"
  }
]
```

#### 3. Get Latest Message
**GET** `/api/v1/chats/{consultationId}/messages/latest`

Retrieve the most recent message in a consultation chat.

**Parameters:**
- `consultationId` (path): UUID of the consultation

**Request Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/chats/123e4567-e89b-12d3-a456-426614174000/messages/latest" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response (200 OK):**
```json
{
  "id": "567e8901-e89b-12d3-a456-426614174002",
  "consultationId": "123e4567-e89b-12d3-a456-426614174000",
  "senderId": 2,
  "receiverId": 1,
  "messageType": "VOICE",
  "messageContent": null,
  "fileUrl": "/hub-files/chat/voice/voice_note.mp3",
  "createdAt": "2024-01-15T10:31:00Z"
}
```

### Error Responses

**400 Bad Request:**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Message content is required for text messages"
}
```

**403 Forbidden:**
```json
{
  "errorCode": "ACCESS_DENIED",
  "message": "Unauthorized to send message in this consultation"
}
```

**404 Not Found:**
```json
{
  "errorCode": "NOT_FOUND",
  "message": "Consultation not found"
}
```

### Business Rules

1. **Consultation Status**: Chat is only allowed when consultation status is `AKTIF` (approved).
2. **User Authorization**: Only Home Owner and Consultant associated with the consultation can access the chat.
3. **File Upload**: Voice notes must be MP3 or M4A format, maximum 10MB.
4. **Message Types**: Support TEXT and VOICE message types.
5. **Real-time**: Currently REST API only; WebSocket implementation pending.

### Data Models

#### ChatMessageDTO
```json
{
  "id": "UUID",
  "consultationId": "UUID",
  "senderId": "Long",
  "receiverId": "Long",
  "messageType": "TEXT|VOICE",
  "messageContent": "String (nullable)",
  "fileUrl": "String (nullable)",
  "createdAt": "Instant"
}
```

## WebSocket Real-Time Chat API (In-Memory Broker)

### Overview
The WebSocket API provides **fully real-time chat functionality** using **STOMP over WebSocket with Spring's built-in simple broker**. No external message broker (RabbitMQ, Kafka, Redis) is required - all communication is handled in-memory within the Spring application.

### Architecture Notes
- **Broker**: Spring WebSocket Simple Broker (in-memory)
- **Protocol**: STOMP over WebSocket
- **Authentication**: JWT token validation on WebSocket handshake
- **Real-time**: Instant message delivery without external dependencies

### WebSocket Configuration
- **Endpoint**: `ws://localhost:8080/ws/chat`
- **STOMP Application Prefix**: `/app`
- **Broker Destinations**:
  - `/topic/chat/{consultationId}` - Chat room messages
  - `/topic/chat/{consultationId}/read` - Read status updates
  - `/queue/notifications/{userId}` - Private notifications

### Authentication
WebSocket connections require JWT authentication passed in the `Authorization` header during the STOMP CONNECT frame.

### STOMP Message Flow

#### 1. Connect to WebSocket
```javascript
const socket = new SockJS('/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect({
    'Authorization': 'Bearer ' + jwtToken
}, function(frame) {
    console.log('Connected: ' + frame);
}, function(error) {
    console.log('Connection error: ' + error);
});
```

#### 2. Subscribe to Chat Room
```javascript
// Subscribe to chat messages
stompClient.subscribe('/topic/chat/' + consultationId, function(message) {
    const chatMessage = JSON.parse(message.body);
    console.log('Received message:', chatMessage);
    // Update UI immediately
});

// Subscribe to read status updates
stompClient.subscribe('/topic/chat/' + consultationId + '/read', function(message) {
    const readStatus = JSON.parse(message.body);
    console.log('Read status:', readStatus);
    // Update read receipts
});

// Subscribe to notifications
stompClient.subscribe('/user/queue/notifications', function(notification) {
    const notificationData = JSON.parse(notification.body);
    console.log('Notification:', notificationData);
    // Show notification badge
});
```

#### 3. Send Messages (Real-time)
```javascript
// Send text message - appears instantly for all subscribers
stompClient.send('/app/chat/' + consultationId + '/send', {}, JSON.stringify({
    consultationId: consultationId,
    senderId: currentUserId,
    receiverId: otherUserId,
    messageType: 'TEXT',
    messageContent: 'Hello, how are you?',
    fileUrl: null
}));

// Send voice message - appears instantly for all subscribers
stompClient.send('/app/chat/' + consultationId + '/send', {}, JSON.stringify({
    consultationId: consultationId,
    senderId: currentUserId,
    receiverId: otherUserId,
    messageType: 'VOICE',
    messageContent: null,
    fileUrl: '/hub-files/chat/voice/audio.mp3'
}));
```

#### 4. Mark Messages as Read (Real-time)
```javascript
// Mark as read - updates instantly for all subscribers
stompClient.send('/app/chat/' + consultationId + '/read/' + messageId, {}, {});
```

### Message Payloads

#### ChatMessagePayload (WebSocket Send)
```json
{
  "consultationId": "UUID",
  "senderId": "Long",
  "receiverId": "Long",
  "messageType": "TEXT|VOICE",
  "messageContent": "String (nullable)",
  "fileUrl": "String (nullable)"
}
```

#### ChatMessageResponse (WebSocket Receive)
```json
{
  "id": "UUID",
  "consultationId": "UUID",
  "senderId": "Long",
  "receiverId": "Long",
  "messageType": "TEXT|VOICE",
  "messageContent": "String (nullable)",
  "fileUrl": "String (nullable)",
  "isRead": "Boolean",
  "createdAt": "Instant"
}
```

#### Notification Payload (WebSocket)
```json
{
  "type": "NEW_MESSAGE",
  "consultationId": "UUID",
  "senderId": "Long",
  "messageType": "TEXT|VOICE",
  "createdAt": "Instant"
}
```

#### Read Status Payload (WebSocket)
```json
{
  "messageId": "UUID",
  "isRead": true,
  "readBy": "Long"
}
```

### Business Rules

1. **Authentication**: JWT token required for WebSocket connection
2. **Authorization**: Only consultation participants can join chat room
3. **Consultation Status**: Only `AKTIF` consultations allow chat
4. **Real-time Updates**: All messages broadcast instantly to `/topic/chat/{consultationId}`
5. **Read Receipts**: Messages marked as read update instantly for all subscribers
6. **Notifications**: New messages trigger instant notifications to receivers
7. **In-Memory**: No external broker - all routing handled by Spring WebSocket

### Error Handling

WebSocket errors are logged server-side. Client should implement reconnection logic:

```javascript
stompClient.onStompError = function(frame) {
    console.log('Broker reported error: ' + frame.headers['message']);
    console.log('Additional details: ' + frame.body);
};

// Reconnection logic
function connect() {
    const socket = new SockJS('/ws/chat');
    stompClient = Stomp.over(socket);
    // ... connection setup
}

stompClient.onWebSocketClose = function() {
    setTimeout(connect, 5000); // Reconnect after 5 seconds
};
```

### Integration with REST API

The WebSocket implementation works alongside the existing REST API:
- **REST API**: Initial data loading, message history, mark as read
- **WebSocket**: Real-time messaging, instant updates
- **Same Business Logic**: Validation and authorization consistent
- **Fallback Support**: REST API works when WebSocket unavailable

### Testing Real-time Functionality

#### 1. Start Application
```bash
mvn spring-boot:run
```

#### 2. Test WebSocket Connection
```javascript
// In browser console or test script
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect({
    'Authorization': 'Bearer YOUR_JWT_TOKEN'
}, function() {
    console.log('Connected to real-time chat!');
});
```

#### 3. Test Message Sending
```javascript
// Send test message
stompClient.send('/app/chat/CONSULTATION_ID/send', {}, JSON.stringify({
    consultationId: 'CONSULTATION_ID',
    senderId: 1,
    receiverId: 2,
    messageType: 'TEXT',
    messageContent: 'Real-time test message!',
    fileUrl: null
}));
```

#### 4. Verify Instant Delivery
- Message should appear immediately in all subscribed clients
- No external broker required
- Uses Spring's in-memory simple broker

#### CreateChatMessageRequest
```json
{
  "messageType": "TEXT|VOICE",
  "messageContent": "String (optional for TEXT)",
  "voiceFile": "MultipartFile (required for VOICE)"
}