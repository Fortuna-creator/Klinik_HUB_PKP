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

## �️ Development Environment Setup Guide

### Step 1: Install Java 21 JDK

#### Windows

1. **Download Java 21 JDK**
   - Go to [Oracle Java 21 Downloads](https://www.oracle.com/java/technologies/downloads/#java21)
   - Select **Windows** → **x64 Installer** (`.exe`)
   - Click download and accept the license agreement

2. **Run Installer**
   - Double-click the downloaded `.exe` file
   - Click **Next** through the installation wizard
   - **Important:** Note the installation path (e.g., `C:\Program Files\Java\jdk-21` or `C:\Program Files\Java\jdk-21.0.1`)
   - Click **Finish**

3. **Set JAVA_HOME Environment Variable**
   - Press `Win + X` → Select **System**
   - Click **Advanced system settings**
   - Click **Environment Variables** button
   - Under "System variables", click **New**
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-21` (or your actual installation path)
   - Click **OK** three times

4. **Update PATH Variable**
   - In "System variables", find **Path** → Click **Edit**
   - Click **New**
   - Add: `%JAVA_HOME%\bin`
   - Click **OK** twice

5. **Verify Installation**
   - Open **Command Prompt** or **PowerShell**
   - Run: `java -version`
   - Should display: `java version "21.x.x"`

#### macOS

```bash
# Using Homebrew (recommended)
brew install java21

# Or download from Oracle and run
# Double-click the .dmg file and follow prompts

# Verify installation
java -version

# Set JAVA_HOME (add to ~/.zprofile or ~/.bash_profile)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

#### Linux (Ubuntu/Debian)

```bash
# Update package manager
sudo apt update

# Install OpenJDK 21
sudo apt install openjdk-21-jdk

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Add to ~/.bashrc for persistence
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
source ~/.bashrc

# Verify
java -version
```

---

### Step 2: Install Apache Maven

#### Windows

1. **Download Maven**
   - Go to [Maven Official Download](https://maven.apache.org/download.cgi)
   - Download the **Binary zip archive** (e.g., `apache-maven-3.9.x-bin.zip`)
   - Extract to a folder (e.g., `C:\Maven\apache-maven-3.9.x`)

2. **Set MAVEN_HOME Environment Variable**
   - Press `Win + X` → Select **System**
   - Click **Advanced system settings**
   - Click **Environment Variables**
   - Under "System variables", click **New**
   - Variable name: `MAVEN_HOME`
   - Variable value: `C:\Maven\apache-maven-3.9.x` (your extraction path)
   - Click **OK**

3. **Update PATH Variable**
   - In "System variables", find **Path** → Click **Edit**
   - Click **New**
   - Add: `%MAVEN_HOME%\bin`
   - Click **OK** twice

4. **Verify Installation**
   - Open **Command Prompt** or **PowerShell**
   - Run: `mvn -version`
   - Should display Maven version and Java version

#### macOS

```bash
# Using Homebrew
brew install maven

# Verify installation
mvn -version

# Check MAVEN_HOME
echo $M2_HOME  # Usually /usr/local/Cellar/maven/x.x.x
```

#### Linux (Ubuntu/Debian)

```bash
# Update package manager
sudo apt update

# Install Maven
sudo apt install maven

# Verify
mvn -version

# MAVEN_HOME is usually /usr/share/maven
```

---

### Step 3: Install PostgreSQL & PgAdmin 4

#### Windows

**Option A: Using PostgreSQL Installer**

1. **Download PostgreSQL**
   - Go to [PostgreSQL Downloads](https://www.postgresql.org/download/windows/)
   - Download the latest stable version (currently 16.x)

2. **Run Installer**
   - Double-click the `.exe` file
   - Set installation directory (e.g., `C:\Program Files\PostgreSQL\16`)
   - Set superuser password (e.g., `postgres`) - **Remember this!**
   - Keep port as default: `5432`
   - Wait for installation to complete

3. **Install PgAdmin 4**
   - After PostgreSQL installation completes, choose to install Stack Builder
   - Select **PgAdmin 4** from the list
   - Follow installation prompts
   - Or download separately from [pgAdmin Downloads](https://www.pgadmin.org/download/)

4. **Verify Installation**
   - Open PgAdmin 4 (from Start menu)
   - It opens in browser: `http://localhost:5050`
   - Login with email you set during installation
   - You should see "Servers" on the left panel

**Option B: Using Docker (Recommended for Development)**

```bash
# Install Docker Desktop from https://www.docker.com/products/docker-desktop

# Run PostgreSQL in Docker
docker run --name postgres-db \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=hub-backend \
  -p 5432:5432 \
  -d postgres:16

# Run PgAdmin 4 in Docker
docker run --name pgadmin \
  -e PGADMIN_DEFAULT_EMAIL=admin@example.com \
  -e PGADMIN_DEFAULT_PASSWORD=admin \
  -p 5050:80 \
  -d dpage/pgadmin4

# Access PgAdmin at http://localhost:5050
```

#### macOS

```bash
# Using Homebrew
brew install postgresql

# Start PostgreSQL service
brew services start postgresql

# Install PgAdmin 4
brew install pgadmin4

# Verify PostgreSQL
psql --version
```

#### Linux (Ubuntu/Debian)

```bash
# Update package manager
sudo apt update

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Install PgAdmin 4
sudo apt install pgadmin4

# Start PostgreSQL service
sudo systemctl start postgresql

# Verify
psql --version
```

---

### Step 4: Create Database & User

1. **Open PgAdmin 4** (http://localhost:5050)
2. **Login** with your credentials
3. **Create Database:**
   - Right-click "Databases" → **Create** → **Database**
   - Name: `hub-backend`
   - Owner: `postgres`
   - Click **Save**

4. **Create Database User (Optional):**
   - Right-click "Login/Group Roles" → **Create** → **Login/Group Role**
   - Name: `hub_user`
   - Password: `secure_password`
   - Privileges tab → Toggle **Can login** to **ON**
   - Click **Save**

5. **Grant Privileges:**
   - Right-click `hub-backend` database → **Properties**
   - Security tab → Grant all privileges to your user
   - Or run SQL:
   ```sql
   GRANT ALL PRIVILEGES ON DATABASE "hub-backend" TO hub_user;
   ```

---

### Step 5: Configure Project & Download Dependencies

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd Klinik_HUB_PKP
   ```

2. **Download Maven Dependencies**
   - Maven automatically downloads dependencies on first build
   - This may take 2-5 minutes on first run
   ```bash
   mvn clean install
   ```

3. **Verify Build**
   ```bash
   mvn clean compile
   ```
   - Should complete without errors

---

## 🚀 Setup & Installation

### 1. Create application.yml Configuration

### 1. Clone Repository

```bash
---

## 🚀 Setup & Installation

### 1. Clone Repository

```bash
git clone <repository-url>
cd Klinik_HUB_PKP
```

### 2. Edit application.yml Configuration File

#### Location
`src/main/resources/application.yml`

#### For Development (PostgreSQL)

**Step 1: Check if PostgreSQL is running**
```bash
# Windows
pg_isready -h localhost -p 5432

# Mac/Linux
pg_isready -h localhost -p 5432
```

**Step 2: Open the file in your editor (VS Code, IntelliJ, etc.)**

**Step 3: Update database configuration**
```yaml
# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /
  compression:
    enabled: true

# Spring Configuration
spring:
  application:
    name: klinik-hub-api
  
  # Database Configuration (PostgreSQL)
  datasource:
    url: jdbc:postgresql://localhost:5432/hub-backend
    username: postgres              # Your PostgreSQL user
    password: password              # Your PostgreSQL password
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
  
  # JPA/Hibernate Configuration
  jpa:
    hibernate:
      ddl-auto: validate            # Use 'validate' for dev (Liquibase handles migrations)
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQL10Dialect
        format_sql: true
        jdbc:
          batch_size: 20
    show-sql: false
    open-in-view: false
  
  # Liquibase Configuration (Database Migrations)
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true
    contexts: dev,test              # Run migrations for dev and test contexts
  
  # Email Configuration (Gmail SMTP)
  mail:
    host: smtp.gmail.com
    port: 587
    username: testingotpasa@gmail.com        # Your Gmail address
    password: your_google_app_password       # Your Gmail app password (16 chars)
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

# JWT Configuration
app:
  jwt:
    secret: your-very-long-secret-key-at-least-32-characters-for-security-dev-only
    expiration: 604800000  # 7 days in milliseconds

# Logging Configuration (Development)
logging:
  level:
    root: INFO
    com.hub.app: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

# Actuator (Health checks)
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: always

# CORS Configuration
app:
  cors:
    allowed-origins: http://localhost:3000,http://localhost:3001,http://127.0.0.1:3000
```

#### Key Points for Development:
- **Database**: Use PostgreSQL (`jdbc:postgresql://localhost:5432/hub-backend`)
- **Database Name**: `hub-backend` (note: use quotes in PostgreSQL for names with hyphens)
- **Username**: Use `postgres` (default PostgreSQL superuser)
- **Password**: The password you set during PostgreSQL installation
- **ddl-auto**: Leave as `validate` (Liquibase handles schema migrations)
- **Mail**: Use your Gmail address and app password
- **JWT Secret**: Use any long string for development
- **Logging Level**: Set to `DEBUG` for detailed logs
- **CORS**: Add your frontend URL (e.g., `http://localhost:3000`)

### 3. Configure Email (Gmail)

**Why?** The application sends OTP codes via email. Without this, email features won't work.

1. **Enable 2-Factor Authentication on Google Account**
   - Go to [myaccount.google.com/security](https://myaccount.google.com/security)
   - Search for "2-Step Verification"
   - Follow the prompts to enable it

2. **Generate App Password**
   - Go to [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
   - Select "Mail" and "Windows Computer" (or your OS)
   - Google generates a 16-character password
   - Copy this password

3. **Update application.yml**
   ```yaml
   spring:
     mail:
       username: your_email@gmail.com        # Your Gmail address
       password: your_16char_app_password      # The password you just copied
   ```

4. **Test Email Sending**
   - Run the application
   - Register a new user
   - Check your email for OTP code

### 4. Verify Prerequisites

Before proceeding, verify all tools are installed:

```bash
# Check Java
java -version
# Output: java version "21.x.x"

# Check Maven
mvn -version
# Output: Maven 3.8.x...

# Check PostgreSQL
psql --version
# Output: psql (PostgreSQL) 16.x

# Check Git (if cloning)
git --version
# Output: git version x.x.x
```

### 5. Download Maven Dependencies

```bash
# Navigate to project directory
cd Klinik_HUB_PKP

# Download all dependencies
mvn clean install

# This may take 2-5 minutes on first run
# It downloads all dependencies from Maven Central Repository
```

### 6. Compile Project

```bash
# Verify everything compiles
mvn clean compile

# Should end with: [INFO] BUILD SUCCESS
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

### Using Maven spring-boot:run (Recommended for Development)

#### Step 1: Ensure PostgreSQL is Running

```bash
# Windows
pg_isready -h localhost -p 5432

# Mac (with Homebrew)
brew services list | grep postgresql

# Linux
sudo systemctl status postgresql

# Docker
docker ps  # Check if postgres container is running
```

#### Step 2: Run the Application

```bash
# From project root directory
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### Step 3: Verify Application is Running

**Check console output for:**
```
[main] o.s.b.w.e.t.TomcatWebServer : Tomcat started on port(s): 8080
[main] c.h.a.KlinikHubPkpApplication : Started KlinikHubPkpApplication
```

**Or test with curl:**
```bash
curl http://localhost:8080/api/health

# Should return:
# {"status":"UP"}
```

---

### Useful Maven Commands

```bash
# Download dependencies only (skip compilation)
mvn dependency:resolve

# Run with debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.root=DEBUG"

# Run specific test class
mvn test -Dtest=YourTestClass

# Build JAR file
mvn clean package

# Skip tests during build
mvn clean install -DskipTests

# Check for dependency conflicts
mvn dependency:tree

# Format code
mvn spotless:apply
```

---

## 🗄️ Database Management

### Connect to PostgreSQL Database

#### Using PgAdmin 4 (GUI)
1. Open browser: `http://localhost:5050`
2. Login with your PgAdmin credentials
3. Left panel → Expand "Servers" → "PostgreSQL 16"
4. Right-click `klinik_hub` → "Query Tool"
5. Run SQL queries

#### Using psql (Command Line)
```bash
# Connect to database
psql -U postgres -d hub-backend

# List all databases
\l

# List all tables
\dt

# Exit
\q

# Run SQL file
psql -U postgres -d hub-backend -f schema.sql
```

### View Liquibase Migration Status

```bash
# See which migrations have been applied
mvn liquibase:status

# Rollback last 2 migrations
mvn liquibase:rollback -Dliquibase.rollbackCount=2

# Generate SQL to apply
mvn liquibase:updateSQL
```

---

## 🔧 Troubleshooting Development Setup

### Issue: "Command not found: mvn"

**Cause:** MAVEN_HOME or PATH not set correctly

**Fix:**
```bash
# Verify MAVEN_HOME is set
echo $MAVEN_HOME  # Mac/Linux
echo %MAVEN_HOME%  # Windows PowerShell

# If empty, re-add to PATH
# Windows: System Properties → Environment Variables
# Mac/Linux: Add to ~/.bashrc or ~/.zprofile, then source it
```

### Issue: "Cannot find JDK"

**Cause:** JAVA_HOME not set

**Fix:**
```bash
# Verify JAVA_HOME
echo $JAVA_HOME    # Mac/Linux
echo %JAVA_HOME%   # Windows PowerShell

# Set JAVA_HOME temporarily (current session only)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home  # Mac
```

### Issue: "Connection refused - 5432"

**Cause:** PostgreSQL not running

**Fix:**
```bash
# Start PostgreSQL
brew services start postgresql  # Mac
sudo systemctl start postgresql  # Linux
# Windows: Start PostgreSQL from Services or PostgreSQL folder

# Or use Docker
docker run -p 5432:5432 -e POSTGRES_PASSWORD=password postgres:16
```

### Issue: "Liquibase: Maven dependencies not found"

**Cause:** Maven not downloaded dependencies

**Fix:**
```bash
# Clean and download dependencies
mvn clean install --update-snapshots

# Or just update
mvn dependency:resolve
```

### Issue: "Port 8080 already in use"

**Cause:** Another app using port 8080

**Fix:**
```bash
# Change port in application.yml
server:
  port: 8081

# Or find and kill process
# Windows PowerShell
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -xi :8080
kill -9 <PID>
```

---

## ▶️ Additional Running Options

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