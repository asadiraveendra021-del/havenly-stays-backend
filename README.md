# Havenly Stays Backend

## 1. Project Overview
Havenly Stays Backend is a Spring Boot REST API for hotel-management-style applications with a production-oriented authentication and authorization system.

It provides:
- User registration and login
- JWT-based stateless authentication
- Refresh token lifecycle management
- Token blacklist support for logout/revocation
- Role-based access control (RBAC)
- Centralized API response and error handling
- Swagger/OpenAPI documentation

In simple terms: users can create accounts, login to receive secure tokens, call protected APIs with JWT, refresh access when needed, and logout safely.

---

## 2. Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Main programming language |
| Spring Boot 4 | Application framework and auto-configuration |
| Spring Security | Authentication, authorization, filter chain, method security |
| JWT (custom HS256 implementation) | Access token format for stateless API auth |
| Spring Data JPA + Hibernate | ORM and persistence layer |
| PostgreSQL | Relational database (configured in `application.properties`) |
| Spring Validation (`jakarta.validation`) | Request DTO validation (`@Valid`, `@Email`, `@NotBlank`) |
| Springdoc OpenAPI / Swagger UI | Interactive API documentation and testing |
| Gradle | Build and dependency management |
| Lombok | Reduces boilerplate (`@Getter`, `@Setter`, `@Builder`, etc.) |

---

## 3. Features Implemented

- Signup (`/api/auth/signup`): Registers a new user and assigns default role `ROLE_USER`.
- Login (`/api/auth/login`): Authenticates credentials and returns access + refresh tokens.
- JWT access token auth: Access token is validated in a custom filter for protected routes.
- Refresh token mechanism (`/api/auth/refresh-token`): Issues a new access token using valid refresh token.
- Logout (`/api/auth/logout`): Blacklists current access token and removes refresh token(s).
- Token blacklist: Revoked tokens are persisted and checked in JWT filter.
- RBAC:
  - `/api/admin/**` -> admin only
  - `/api/user/**` -> user or admin
- Global exception handling (`@RestControllerAdvice`) with structured error payload.
- Login rate limiting: max 5 login attempts per minute per IP.
- Consistent response wrapper (`ApiResponse<T>`) for normal API responses.

---

## 4. Authentication Flow

### Signup Flow
1. Client sends `name`, `email`, `password` to `/api/auth/signup`.
2. System validates input.
3. If email is new, password is hashed with BCrypt.
4. User is saved with `ROLE_USER`.
5. API returns `201` with user details in `ApiResponse<UserResponse>`.

### Login Flow
1. Client sends `email`, `password` to `/api/auth/login`.
2. Rate limiter checks IP request frequency.
3. Credentials are validated.
4. System generates:
   - Access Token (JWT, 15 minutes)
   - Refresh Token (UUID, stored in DB, 7 days)
5. API returns both tokens in `ApiResponse<TokenRefreshResponse>`.

### Access Token Usage
1. Client sends header: `Authorization: Bearer <access-token>`.
2. JWT filter extracts and validates token signature + expiration.
3. Filter checks token blacklist.
4. If valid, authentication is set in `SecurityContext`.
5. Protected endpoint executes.

### Refresh Flow
1. Client sends refresh token to `/api/auth/refresh-token`.
2. Backend finds token in DB and verifies expiration.
3. If valid, a new access token is issued.
4. Response contains new access token and refresh token.

### Logout Flow
1. Client calls `/api/auth/logout` with refresh token body and bearer access token header.
2. Access token is added to blacklist (with expiry).
3. Refresh token record is deleted by user.
4. User session is effectively invalidated.

---

## 5. JWT Token Explanation
JWT (JSON Web Token) is a compact token made of three parts:

- Header: token metadata (`alg`, `typ`)
- Payload: claims like subject (`sub`), issued-at (`iat`), expiry (`exp`)
- Signature: cryptographic signature using server secret (HS256)

In this project:
- `sub` stores user email.
- JWT is generated in `security/JwtTokenProvider`.
- Each request token is validated in `security/JwtAuthenticationFilter`.
- Invalid/expired/blacklisted tokens are not authenticated.

---

## 6. Refresh Token Explanation
Refresh tokens are long-lived credentials used only to get new access tokens.

Why needed:
- Access tokens are short-lived for security.
- Users should not login repeatedly after every access token expiry.

Difference:
- Access token: short-lived, sent on every protected API request.
- Refresh token: long-lived, used only on `/api/auth/refresh-token`.

Storage and validation:
- Refresh token is a UUID.
- Stored in DB (`refresh_tokens` table).
- Expiry is checked before issuing new access tokens.
- Expired refresh tokens are removed and rejected.

Security benefits:
- Limits impact window if access token is leaked.
- Refresh tokens can be revoked from server side.

---

## 7. Role-Based Access Control (RBAC)
Roles implemented:
- `ROLE_USER`
- `ROLE_ADMIN`

How it is enforced:
- URL rules in `SecurityConfig`:
  - `/api/admin/**` -> `hasRole("ADMIN")`
  - `/api/user/**` -> `hasAnyRole("USER","ADMIN")`
- Method-level annotations:
  - `@PreAuthorize("hasRole('ADMIN')")`
  - `@PreAuthorize("hasAnyRole('USER','ADMIN')")`

Note:
- `@RolesAllowed` is another valid RBAC annotation in Jakarta/Spring ecosystems, but this project currently uses `@PreAuthorize`.

---

## 8. API Endpoints

| Endpoint | Method | Description | Authentication Required |
|---|---|---|---|
| `/api/auth/signup` | POST | Register new user | No |
| `/api/auth/login` | POST | Login and get access + refresh token | No |
| `/api/auth/refresh-token` | POST | Generate new access token using refresh token | No |
| `/api/auth/logout` | POST | Logout and revoke tokens | Yes (Bearer token + refresh token body) |
| `/api/user/health` | GET | Sample user-protected endpoint | Yes (`ROLE_USER` or `ROLE_ADMIN`) |
| `/api/admin/health` | GET | Sample admin-only endpoint | Yes (`ROLE_ADMIN`) |

### Standard Success Response
```json
{
  "timestamp": "2026-03-08T12:00:00Z",
  "status": 200,
  "message": "Authentication successful",
  "data": {}
}
```

### Standard Error Response
```json
{
  "timestamp": "2026-03-08T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}
```

---

## 9. Swagger API Documentation
After app startup:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

How to authorize in Swagger:
1. Login and copy `accessToken`.
2. Click `Authorize`.
3. Enter `Bearer <accessToken>`.
4. Call protected endpoints.

Public endpoints in Swagger:
- `/api/auth/signup`
- `/api/auth/login`
- `/api/auth/refresh-token`

---

## 10. Project Structure

```text
src/main/java/com/asadi/havenly_stays
├── config       # Bean and OpenAPI configuration
├── controller   # REST controllers (auth, admin, user)
├── dto          # Request/response wrappers and payload models
├── entity       # JPA entities (User, Role, RefreshToken, BlacklistedToken)
├── exception    # Custom exceptions and global exception handler
├── repository   # Spring Data JPA repositories
├── security     # JWT provider, auth filter, security config, user details service
├── service      # Service interfaces
├── service/impl # Business logic implementations
└── util         # Utility classes (e.g., login rate limiter)
```

Layer responsibilities:
- `controller`: HTTP contract and status codes
- `service`: business workflow
- `repository`: data access
- `security`: authentication/authorization internals

---

## 11. Security Implementation
- BCrypt password hashing (`PasswordEncoder` bean).
- Stateless authentication (`SessionCreationPolicy.STATELESS`).
- JWT signature and expiry validation on each request.
- Blacklist check before authentication acceptance.
- Refresh token expiry validation and server-side revocation.
- Scheduled cleanup for expired refresh/blacklisted tokens.
- Login brute-force protection via per-IP rate limiting.

---

## 12. How to Run the Project

### Prerequisites
- Java 17+
- PostgreSQL running locally
- Gradle (or use wrapper)

### Steps
1. Clone repository:
   ```bash
   git clone <your-repo-url>
   cd havenly-stays-backend
   ```
2. Create database:
   ```sql
   CREATE DATABASE havenly_stays;
   ```
3. Update DB/JWT settings in `src/main/resources/application.properties`.
4. Run app:
   ```bash
   ./gradlew bootRun
   ```
   On Windows:
   ```powershell
   .\gradlew.bat bootRun
   ```
5. Open Swagger UI at:
   `http://localhost:8080/swagger-ui/index.html`

---

## 13. Example API Usage

### Signup
Request:
```http
POST /api/auth/signup
Content-Type: application/json
```
```json
{
  "name": "Ravin Kumar",
  "email": "ravin@example.com",
  "password": "strongPass123"
}
```

Response:
```json
{
  "timestamp": "2026-03-08T12:00:00Z",
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "name": "Ravin Kumar",
    "email": "ravin@example.com"
  }
}
```

### Login
Request:
```http
POST /api/auth/login
Content-Type: application/json
```
```json
{
  "email": "ravin@example.com",
  "password": "strongPass123"
}
```

Response:
```json
{
  "timestamp": "2026-03-08T12:00:00Z",
  "status": 200,
  "message": "Authentication successful",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "6b6ebf1a-21d9-4022-bb5a-8a4f9e31e986",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

### Refresh Token
Request:
```http
POST /api/auth/refresh-token
Content-Type: application/json
```
```json
{
  "refreshToken": "6b6ebf1a-21d9-4022-bb5a-8a4f9e31e986"
}
```

Response:
```json
{
  "timestamp": "2026-03-08T12:10:00Z",
  "status": 200,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJ...new...",
    "refreshToken": "6b6ebf1a-21d9-4022-bb5a-8a4f9e31e986",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

---

## 14. Future Improvements
- OAuth2 / Google login
- Email verification flow
- Password reset workflow
- Account lockout + captcha after repeated failures
- Redis-based rate limiting and token cache
- Key rotation for JWT signing
- Multi-device refresh token management

---

## 15. Contribution
Contributions are welcome.

Suggested flow:
1. Fork the repository
2. Create a feature branch
3. Commit clean, focused changes
4. Add/update tests
5. Open a pull request with clear description

Recommended checks before PR:
```bash
./gradlew clean test
./gradlew compileJava
```

---

## 16. License
This project currently has no explicit license file.

Add a license (for example MIT, Apache-2.0, or GPL) before public distribution.
