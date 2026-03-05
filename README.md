# 🏥 Klinik HUB PKP - Backend API

A comprehensive Spring Boot backend for managing online and offline consultations, featuring OTP-based authentication, session-based consultation booking, and role-based access control for homeowners and Balai staff.

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Build](#build)
- [Running the Application](#running-the-application)
- [Environment Variables](#environment-variables)
- [Database Migrations](#database-migrations)
- [API Documentation](#api-documentation)
- [Test Data](#test-data)
- [Security Features](#security-features)
- [Common Issues](#common-issues)

---

## 🎯 Overview

**Klinik HUB PKP** is a consultation management platform that enables:

- **Homeowners (Warga)**: Book consultations, verify sessions, track consultation history
- **Balai Staff**: Manage consultation requests, confirm/reject/complete consultations
- **Security**: HttpOnly JWT authentication, OTP verification, rate limiting

### Key Features

✅ OTP-based email registration (6-digit, rate-limited)  
✅ HttpOnly cookie authentication (XSS/CSRF protected)  
✅ 3-attempt OTP verification lockout (15 minutes)  
✅ Online & offline consultation support  
✅ Real-time chat messaging  
✅ File upload management  
✅ Consultation lifecycle tracking  
✅ Role-based access control (HOMEOWNER, BALAI)  

---

## 🛠 Tech Stack

| Component | Version |
|-----------|---------|
| **Java** | 21 (LTS) |
| **Spring Boot** | 3.x |
| **Database** | MySQL 8.0+ |
| **Build Tool** | Maven |
| **ORM** | JPA/Hibernate |
| **Mail** | Gmail SMTP |
| **UUID Storage** | Binary(16) |

---

## 📦 Prerequisites

### Required
- **Java 21 JDK** - [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** - [Download](https://dev.mysql.com/downloads/mysql/)
- **Git** (optional, for cloning)

### Optional
- **Docker** (if using containerization)
- **Postman** (for API testing)

---

## 🚀 Setup & Installation

### 1. Clone Repository

```bash
git clone <repository-url>
cd Klinik_HUB_PKP
```

### 2. Create MySQL Database

```sql
-- Open MySQL and create the database
CREATE DATABASE klinik_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Ensure MySQL user has permissions
GRANT ALL PRIVILEGES ON klinik_hub.* TO 'your_username'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Create `application-local.yml`

In `src/main/resources/`:

```yaml
# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /

# Spring Configuration
spring:
  application:
    name: klinik-hub-api
  
  # Database Configuration
  datasource:
    url: jdbc:mysql://localhost:3306/klinik_hub?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
    username: your_mysql_username
    password: your_mysql_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # Use 'validate' in prod, 'update' in dev
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
    show-sql: false
  
  # Liquibase Configuration (Database Migrations)
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true
  
  # Email Configuration
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:testingotpasa@gmail.com}
    password: ${MAIL_PASSWORD:your_google_app_password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
        smtp:
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
  
  # JPA/Hibernate
  jpa:
    open-in-view: false

# JWT Configuration
app:
  jwt:
    secret: your-very-long-secret-key-at-least-32-characters-for-security
    expiration: 604800000  # 7 days in milliseconds

# Logging
logging:
  level:
    root: INFO
    com.hub.app: DEBUG
    org.springframework.security: DEBUG

# Actuator (Health checks)
management:
  endpoints:
    web:
      exposure:
        include: health,metrics
  endpoint:
    health:
      show-details: always
```

### 4. Set Environment Variables (Production)

```bash
# Linux/Mac
export MAIL_USERNAME="your_email@gmail.com"
export MAIL_PASSWORD="your_google_app_password"
export JWT_SECRET="your-long-secret-key"
export SPRING_DATASOURCE_PASSWORD="mysql_password"

# Windows PowerShell
$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_google_app_password"
$env:JWT_SECRET="your-long-secret-key"
$env:SPRING_DATASOURCE_PASSWORD="mysql_password"
```

---

## 🔨 Build

### Clean Build
```bash
mvn clean install
```

### Build Without Tests
```bash
mvn clean install -DskipTests
```

### Compile Only
```bash
mvn clean compile
```

### Check for Errors
```bash
mvn compile -e  # Show errors in detail
```

---

## ▶️ Running the Application

### Option 1: Maven (Development)

```bash
# Run with default profile (application.yml)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

### Option 2: Java Command

```bash
# Build first
mvn clean package -DskipTests

# Run the generated JAR
java -jar target/klinik-hub-api-1.0.jar
```

### Option 3: IDE (IntelliJ IDEA)

1. **Open Project** → Select project root
2. **Run** → Select `KlinikHubPkpApplication` main class
3. **Click** ▶️ Run button

### Option 4: Docker

```bash
# Build Docker image
docker build -t klinik-hub-api .

# Run container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/klinik_hub \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e MAIL_USERNAME=your_email@gmail.com \
  -e MAIL_PASSWORD=your_app_password \
  klinik-hub-api
```

### Verify Application Started

```bash
# Check health endpoint
curl http://localhost:8080/health

# Expected response
{"status":"UP"}
```

---

## 🔐 Environment Variables

| Variable | Required | Default | Purpose |
|----------|----------|---------|---------|
| `SPRING_DATASOURCE_URL` | ✅ | `jdbc:mysql://localhost:3306/klinik_hub` | MySQL connection |
| `SPRING_DATASOURCE_USERNAME` | ✅ | `root` | MySQL username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | - | MySQL password |
| `MAIL_USERNAME` | ✅ | - | Gmail address for OTP emails |
| `MAIL_PASSWORD` | ✅ | - | Gmail App Password (16 chars) |
| `JWT_SECRET` | ✅ | - | JWT signing secret (32+ chars) |
| `SPRING_PROFILES_ACTIVE` | ❌ | `dev` | Profile: dev, prod, staging |

### Gmail Setup for OTP

1. **Enable 2-Factor Authentication** on Google Account
2. **Generate App Password**:
   - Go to [Google Account Security](https://myaccount.google.com/security)
   - App passwords → Select "Mail" & "Windows" → Generate
   - Copy 16-character password
   - Set as `MAIL_PASSWORD` environment variable

---

## 🗄️ Database Migrations

### Automatic (On Startup)
Liquibase automatically runs migrations from `src/main/resources/db/changelog/`:

```
001-initial-schema.sql
002-update-schema.sql
...
042-otp-attempts-rate-limiting.sql
db.changelog-master.xml
```

### Manual Migration
```bash
# Show migration status
mvn liquibase:status

# Rollback last change
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secure_password",
  "fullName": "John Doe",
  "phone": "08123456789",
  "address": "Jl. Sample No. 123",
  "nik": "3201201234567890",
  "role": "Warga",
  "username": "johndoe"
}
```

**Response:** 200 OK - User created, OTP sent to email

#### Verify OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json
Cookie: auth_token=<jwt>

{
  "email": "user@example.com",
  "otpCode": "123456"
}
```

**Response:** 200 OK - User verified, HttpOnly cookie set with JWT

**Test OTP:** `123456`

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password"
}
```

**Response:** 200 OK - HttpOnly cookie set with JWT

#### Logout
```http
POST /api/auth/logout
Cookie: auth_token=<jwt>
```

**Response:** 200 OK - Cookie cleared

#### Resend OTP
```http
POST /api/auth/resend-otp?email=user@example.com
```

---

### Consultation Endpoints (Require: HOMEOWNER)

#### Book Consultation
```http
POST /api/consultations/book
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "consultantId": 1,
  "consultationType": "ONLINE",
  "requestedDate": "2026-03-10",
  "description": "Kitchen renovation consultation"
}
```

#### Get My Consultations
```http
GET /api/consultations/me
Authorization: Bearer <jwt>
```

#### Get Confirmed Consultations (Inbox)
```http
GET /api/confirmations/inbox
Authorization: Bearer <jwt>
```

**Response includes:** `sessionKey` (8-char verification code)

#### Verify Consultation Session
```http
POST /api/consultations/{id}/verify
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "sessionKey": "ABC123XZ"
}
```

---

### Balai Endpoints (Require: BALAI)

#### Get Dashboard Consultations
```http
GET /api/dashboard/consultations
Authorization: Bearer <jwt>
```

#### Confirm Consultation
```http
PUT /api/consultations/{id}/confirm
Authorization: Bearer <jwt>
```

**Returns:** System-generated `sessionKey` for homeowner

#### Reject Consultation
```http
PUT /api/consultations/{id}/reject
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "reason": "Time not available"
}
```

#### Complete Consultation
```http
PUT /api/consultations/{id}/complete
Authorization: Bearer <jwt>
```

#### Get Statistics
```http
GET /api/dashboard/stats
Authorization: Bearer <jwt>
```

---

### Public Endpoints

#### Get All Balai Offices
```http
GET /api/balai/list
```

#### Get Available Slots
```http
GET /api/balai/{balaiId}/available-slots?date=2026-03-06
Authorization: Bearer <jwt>
```

---

## 👥 Test Data

### Homeowners (ID: 5001-5005)

| Email | Name | Password |
|-------|------|----------|
| homeowner1@test.com | Budi Santoso | password |
| homeowner2@test.com | Siti Nurhaliza | password |
| homeowner3@test.com | Ahmad Wijaya | password |
| homeowner4@test.com | Dewi Lestari | password |
| homeowner5@test.com | Rany Hermawan | password |

### Balai Staff (ID: 6001-6005)

| Email | Name | Password |
|-------|------|----------|
| balai.jakarta@test.com | Hadi Kurniawan | password |
| balai.bandung@test.com | Eka Purnama | password |
| balai.surabaya@test.com | Bambang Sutrisno | password |
| balai.yogya@test.com | Nur Cahyo | password |
| balai.semarang@test.com | Listio Wicaksono | password |

### Test Credentials

| Field | Value |
|-------|-------|
| OTP | 123456 |
| Password | password |
| Session Key Format | 8 chars (e.g., ABC123XZ) |

---

## 🔐 Security Features

### Authentication
- ✅ **HttpOnly Cookies** - JWT stored in secure, HTTP-only cookies
- ✅ **SameSite=Strict** - CSRF protection
- ✅ **JWT Validation** - Token signature verification on each request
- ✅ **Role-Based Access Control** - HOMEOWNER, BALAI roles

### OTP Security
- ✅ **Rate Limiting** - Max 3 generation attempts per hour
- ✅ **Verification Lockout** - 3 failed attempts → 15-minute lockout
- ✅ **6-Digit Code** - 1 million combinations
- ✅ **10-Minute Expiry** - OTP expires after 10 minutes

### Session Security
- ✅ **System-Generated Keys** - 8-character alphanumeric codes
- ✅ **Cannot Be Bypassed** - Balai staff cannot provide their own key
- ✅ **Prevents Fraud** - Key must match user's session

### CORS
- ✅ **Credential Support** - `Access-Control-Allow-Credentials: true`
- ✅ **Whitelisted Origins** - Only allowed frontends
- ✅ **Preflight Handling** - OPTIONS requests properly handled

### Password
- ✅ **BCrypt Hashing** - Latest Spring Security encoder
- ✅ **10 Round Salt** - Resistant to brute force attacks

---

## 🐛 Common Issues

### Issue: "Cannot find symbol" on compile

**Cause:** New classes haven't been compiled yet

**Fix:**
```bash
mvn clean compile
```

### Issue: 401 Unauthorized on API calls

**Cause:** JWT token missing or invalid

**Solution:**
1. Log in first: `POST /api/auth/login`
2. Cookie is automatically set
3. Use `credentials: 'include'` in frontend fetch
4. Retry the request

### Issue: CORS error - "Access-Control-Allow-Origin"

**Cause:** Browser blocking cross-origin requests

**Solution:**
- Backend CORS configured in `SecurityConfig.java`
- Frontend must use `credentials: 'include'`
- Check allowed origins in environment

### Issue: OTP not sending emails

**Cause:** Gmail credentials not set or incorrect

**Solution:**
1. Verify `MAIL_USERNAME` is set (Gmail address)
2. Generate App Password on Google Account
3. Set `MAIL_PASSWORD` to 16-character app password
4. Enable 2FA on Google Account

### Issue: Database connection refused

**Cause:** MySQL not running or wrong credentials

**Solution:**
```bash
# Start MySQL
brew services start mysql  # Mac
sudo service mysql start   # Linux
# or use Docker:
docker run -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password mysql:8.0

# Verify connection
mysql -h localhost -u root -p
```

### Issue: Port 8080 already in use

**Cause:** Another application using port 8080

**Solution:**
```bash
# Change port in application.yml
server:
  port: 8081

# Or kill process on port 8080
# Mac/Linux:
lsof -ti:8080 | xargs kill -9

# Windows PowerShell:
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process
```

---

## � Documentation Reference

### Core API Documentation
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Complete API endpoints reference with authentication, OTP verification, and rate limiting details
- **[API_CONTRACT.md](API_CONTRACT.md)** - Frontend integration guide with comprehensive endpoint specifications for contract management and consultation workflows

### Consultation & Lifecycle Management
- **[CONSULTATION_LIFECYCLE_DOCUMENTATION.md](CONSULTATION_LIFECYCLE_DOCUMENTATION.md)** - Complete consultation workflow with status transitions, security details, and authentication requirements
- **[OFFLINE_CONSULTATION_BACKEND_DOCUMENTATION.md](OFFLINE_CONSULTATION_BACKEND_DOCUMENTATION.md)** - Offline consultation scheduling module with idempotency patterns, state machine, and dashboard features
- **[PAYMENT_LIFECYCLE_DOCUMENTATION.md](PAYMENT_LIFECYCLE_DOCUMENTATION.md)** - Payment processing workflow, installment handling, and financial state management

### Contract Management
- **[CONTRACT_API_DOCUMENTATION.md](CONTRACT_API_DOCUMENTATION.md)** - Complete contract lifecycle API with file upload/download operations, signature management, and approval workflows

### Testing & Integration
- **[POSTMAN_TESTING_GUIDE.md](POSTMAN_TESTING_GUIDE.md)** - Comprehensive Postman testing guide with HttpOnly cookie authentication, OTP rate limiting test scenarios, and collection setup instructions
- **[sample-requests-responses.md](sample-requests-responses.md)** - Sample API requests and responses for all endpoints with examples

### Postman Collections
- **Hub_API_v2.1_Postman_Collection.json** - Complete Postman collection with all endpoints, authentication type, test scripts, and environment variables pre-configured
- **Hub_Auth_Postman_Collection.json** - Authentication module testing
- **Hub_File_Operations_Postman_Collection.json** - File upload/download operations

---

## 🔍 Quick Reference

### For Frontend Developers
1. Start with **[API_CONTRACT.md](API_CONTRACT.md)** for integration specifications
2. Review **[POSTMAN_TESTING_GUIDE.md](POSTMAN_TESTING_GUIDE.md)** for testing approach
3. Check **[CONSULTATION_LIFECYCLE_DOCUMENTATION.md](CONSULTATION_LIFECYCLE_DOCUMENTATION.md)** for workflow states
4. Import Postman collection for quick API testing

### For Backend Developers
1. Review **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** for endpoint details
2. Check **[OFFLINE_CONSULTATION_BACKEND_DOCUMENTATION.md](OFFLINE_CONSULTATION_BACKEND_DOCUMENTATION.md)** for module architecture
3. Study **[CONTRACT_API_DOCUMENTATION.md](CONTRACT_API_DOCUMENTATION.md)** for contract management implementation
4. Verify **[CONSULTATION_LIFECYCLE_DOCUMENTATION.md](CONSULTATION_LIFECYCLE_DOCUMENTATION.md)** for state management

### For QA/Testing
1. Import **Hub_API_v2.1_Postman_Collection.json** into Postman
2. Follow **[POSTMAN_TESTING_GUIDE.md](POSTMAN_TESTING_GUIDE.md)** for test workflows
3. Use test data from this README (Test Data section)
4. Review **[sample-requests-responses.md](sample-requests-responses.md)** for expected responses

---

## 💡 Key Implementation Notes

### Authentication
- ✅ Uses **HttpOnly Cookie-based JWT** (not localStorage)
- ✅ Automatically set after `/api/auth/verify-otp` or `/api/auth/login`
- ✅ Frontend must use `credentials: 'include'` in fetch calls
- ✅ Token valid for **7 days**

### OTP Rate Limiting
- ✅ **Generation limit**: 3 per hour (returns HTTP 429)
- ✅ **Verification limit**: 3 failed attempts = 15-minute lockout (returns HTTP 429)
- ✅ Returns `retryAfterSeconds` field in 429 response
- ✅ Test OTP code: `123456`

### Session Keys
- ✅ System-generated 8-character alphanumeric codes
- ✅ Cannot be provided by Balai staff (anti-fraud measure)
- ✅ Unique per consultation confirmation

---

## 📄 License

Internal use only. All rights reserved.

---

**Last Updated:** March 6, 2026  
**Backend Version:** 2.1  
**Status:** 🟢 Production Ready