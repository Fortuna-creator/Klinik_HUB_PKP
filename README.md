# hub-backend

This repository contains the backend for the Hub application (Spring Boot).

What I created
- Starter package structure under `src/main/java/com/hub/app` with module folders (auth, user, consultation, etc.).
- Minimal `pom.xml` with Spring Boot, JPA, Security, OpenAPI, MinIO and jjwt dependencies.
- `HubApplication.java` (main entry) and `HealthController` (`GET /health`).
- Auth skeleton: `User` entity, DTOs, `UserRepository`, `AuthService`, `AuthController` (`/auth/register`, `/auth/login`), and `JwtUtil`.
- Config placeholders in `src/main/java/com/hub/app/config`.
- `src/main/resources/application.yml` with H2/MinIO/JWT placeholders and `schema.sql`.

Quick start (Windows PowerShell)

1) Build (requires Maven on PATH):

```powershell
cd c:/Users/dwiok/OneDrive/Desktop/hub-pkp/backend/hub-backend
mvn -DskipTests package
```

2) Run:

```powershell
mvn spring-boot:run
```

3) Health check:

```powershell
curl http://localhost:8080/health
```

Auth endpoints (development)
- POST /auth/register  { "username": "user", "password": "pass" } -> returns { "token": "..." }
- POST /auth/login     { "username": "user", "password": "pass" } -> returns { "token": "..." }

Notes & next steps
- The current `SecurityConfig` exposes `/auth/**` and `/health` and registers a `PasswordEncoder` bean. JWT validation filter is not yet wired into the filter chain.
- `JwtUtil` uses the `jwt.secret` from `application.yml`. For production, use a stronger secret and rotate keys.
- Implement controllers, services, repositories and DTOs for other modules (user, consultation, portfolio, etc.).
- Consider adding a Maven wrapper (`mvnw`) and a CI pipeline so builds work even if Maven isn't installed locally.

If you want, I can:
- Add a simple health+auth test and run the build in CI.
- Add `mvnw` (Maven wrapper) so you can build without installing Maven.
- Wire JWT authentication filter into Spring Security and add role-based access checks.
