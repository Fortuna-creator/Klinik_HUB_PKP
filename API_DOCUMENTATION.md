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