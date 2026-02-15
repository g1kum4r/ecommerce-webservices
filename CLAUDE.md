# Claude Agent Context - E-Commerce Webservices

> **Purpose**: This document is optimized for Claude AI agents to efficiently understand the codebase, interpret user queries, and generate accurate, context-aware responses for development assistance.

---

## Quick Reference

**Project Type**: Spring Boot 4.0.2 + Kotlin 2.2.21 | **Architecture**: Modular Monolith (Spring Modulith-ready)
**Database**: PostgreSQL 16 (JDBC) + Liquibase 5.0.1 | **Cache**: Redis 7 | **Auth**: JWT + OAuth2 + BCrypt | **Build**: Gradle 9.3.0

### Critical File Locations
```
Controllers:     src/main/kotlin/lakho/ecommerce/webservices/{module}/api/
Services:        src/main/kotlin/lakho/ecommerce/webservices/{module}/services/
Repositories:    src/main/kotlin/lakho/ecommerce/webservices/{module}/repositories/
Entities:        src/main/kotlin/lakho/ecommerce/webservices/{module}/repositories/entities/
Security:        src/main/kotlin/lakho/ecommerce/webservices/config/SecurityConfig.kt
JWT Filter:      src/main/kotlin/lakho/ecommerce/webservices/config/JwtAuthenticationFilter.kt
Exception:       src/main/kotlin/lakho/ecommerce/webservices/config/GlobalExceptionHandler.kt
OpenAPI:         src/main/kotlin/lakho/ecommerce/webservices/config/OpenApiConfiguration.kt
Migrations:      src/main/resources/db/changelog/
Config:          src/main/resources/application.properties
Environment:     .env (use .env.example as template)
Docker:          docker-compose.yml
```

---

## Understanding User Queries - Decision Tree

### Query Classification

**When user mentions:**

| Keyword | Module | Files to Check |
|---------|--------|----------------|
| "login", "register", "token", "JWT" | `auth` | `AuthController.kt`, `JwtService.kt`, `AuthService.kt` |
| "OAuth2", "Google login", "social login" | `auth` + `config` | `OAuth2AuthenticationSuccessHandler.kt`, `SecurityConfig.kt` |
| "user", "password", "profile" | `user` | `User.kt`, `UserRepository.kt`, `UserService.kt` |
| "role", "permission", "access" | `user` + `config` | `Role.kt`, `UserRole.kt`, `SecurityConfig.kt` |
| "admin" | `admin` | `AdminController.kt`, `AdminService.kt` |
| "store" | `store` | `StoreController.kt`, `StoreService.kt` |
| "consumer", "customer" | `consumer` | `ConsumerController.kt`, `ConsumerService.kt` |
| "endpoint", "API", "controller" | Layer: `api/` | `{Module}Controller.kt` |
| "business logic", "service" | Layer: `services/` | `{Module}Service.kt` |
| "database", "table", "query" | Layer: `repositories/` + migrations | Entity files + SQL migrations |
| "authentication", "authorization" | Security | `SecurityConfig.kt`, `JwtAuthenticationFilter.kt` |
| "exception", "error handling" | Config | `GlobalExceptionHandler.kt` |
| "Swagger", "OpenAPI", "API docs" | Config | `OpenApiConfiguration.kt` |

### Response Strategy by Query Type

#### 1. **"Add a new feature"**
**Steps:**
1. Identify target module (auth/user/admin/store/consumer)
2. Determine layers needed (usually all 3: controller → service → repository)
3. Check if database changes required → Create Liquibase migration
4. Update `SecurityConfig.kt` for authorization rules
5. Follow existing patterns in the module

**Example Response Flow:**
```
User: "Add order management for stores"
Claude Action:
  ✓ Module: store
  ✓ Create: OrderController.kt (api/), OrderService.kt (services/), OrderRepository.kt (repositories/)
  ✓ Create: Order.kt entity in repositories/entities/
  ✓ Create: SQL migration in db/changelog/v1.1.0/store/create-orders-table.sql
  ✓ Update: SecurityConfig.kt to add /api/store/orders/** → hasRole("STORE")
  ✓ Pattern: Follow existing StoreController structure
```

#### 2. **"Fix authentication/authorization issue"**
**Investigation Order:**
1. `JwtAuthenticationFilter.kt` - Token extraction and validation
2. `JwtService.kt` - Token creation/parsing logic
3. `SecurityConfig.kt` - Authorization rules
4. `AuthService.kt` - Login/registration logic
5. Check environment: `JWT_SECRET` consistency

**Common Causes:**
- Token expired (check `JWT_ACCESS_TOKEN_EXPIRATION_MS`)
- Role mismatch (DB has "ADMIN" but endpoint checks "ROLE_ADMIN")
- Filter order incorrect (JWT filter must run before Spring Security filter)

#### 3. **"Add/modify database schema"**
**Required Actions:**
1. **Never modify existing migrations** - Always create new changesets
2. Determine version: Use latest or create new (v1.1.0, v1.2.0)
3. Create feature folder: `v{version}/{feature}/`
4. Create changelog: `changelog-{feature}.yaml`
5. Write SQL: Separate files per table/operation
6. Update entity: Modify corresponding `.kt` entity file
7. Test: Ensure Liquibase applies changes on startup

#### 4. **"Add unit tests" or "Add integration tests"**
**Testing Strategy:**
1. **Unit Tests** - For service layer (business logic)
   - Location: `src/test/kotlin/lakho/ecommerce/webservices/{module}/services/`
   - Mock all dependencies (repositories, external services)
   - Test business logic in isolation
   - Use JUnit 5 + Mockito
   - Follow pattern: `{ServiceName}Test.kt`

2. **Integration Tests** - For API layer (controllers)
   - Location: `src/test/kotlin/lakho/ecommerce/webservices/{module}/api/`
   - Test full API flows (controller → service → repository)
   - Use Testcontainers for PostgreSQL
   - Test with Spring Security enabled
   - Verify authentication and authorization
   - Follow pattern: `{ControllerName}IntegrationTest.kt`

**Required After Creating APIs/Services:**
- **ALWAYS add unit tests to service layers** - Test business logic with mocked dependencies
- **ALWAYS add integration tests to API layers** - Test endpoints with authentication/authorization

**Migration Template:**
```yaml
# src/main/resources/db/changelog/v1.0.0/myfeature/changelog-myfeature.yaml
databaseChangeLog:
  - changeSet:
      id: "myfeature-tables.v1"
      author: "G1"
      failOnError: true
      runOnChange: true
      changes:
        - sqlFile:
            path: create-myfeature-table.sql
            relativeToChangelogFile: true
```

#### 5. **"How does X work?"**
**Reference Sections:**
- Authentication flow → See "JWT Authentication Flow" below
- Role system → See "Role-Based Authorization" below
- Module structure → See "Module Architecture" below
- Database migrations → See "Liquibase Pattern" below
- Testing → See "Testing Strategy" section

---

## Architecture Quick Reference

### Module Structure (Domain-Driven)

```
lakho.ecommerce.webservices/
│
├── auth/               # Handles JWT token generation, login, registration
│   ├── api/            # AuthController (public endpoints)
│   ├── services/       # AuthService (business logic), JwtService (token ops)
│   └── JwtProperties   # Configuration binding (@ConfigurationProperties)
│
├── user/               # Core user domain (entities, repositories)
│   ├── repositories/   # UserRepository, RoleRepository, UserRoleRepository
│   │   └── entities/   # User, Role, UserRole (database models)
│   ├── services/       # UserService (user operations)
│   └── package-info.java  # Spring Modulith module definition
│
├── admin/              # Admin-specific operations (user management)
├── store/              # Store-specific operations (inventory, orders)
├── consumer/           # Consumer-specific operations (shopping, reviews)
│
└── config/             # Cross-cutting concerns
    ├── SecurityConfig.kt           # Spring Security + authorization rules
    ├── JwtAuthenticationFilter.kt  # JWT validation filter
    └── OpenApiConfiguration.kt     # Swagger/OpenAPI setup
```

### 3-Layer Pattern (Within Each Module)

```
┌─────────────────────────────────────┐
│  api/ (Presentation Layer)          │  ← REST Controllers, DTOs (Request/Response models)
│  - Handles HTTP requests/responses  │
│  - Input validation                 │
│  - Delegates to services             │
└───────────────┬─────────────────────┘
                │
┌───────────────▼─────────────────────┐
│  services/ (Business Logic Layer)   │  ← Services
│  - Business rules & workflows       │
│  - Transaction management           │
│  - Delegates to repositories        │
└───────────────┬─────────────────────┘
                │
┌───────────────▼─────────────────────┐
│  repositories/ (Data Access Layer)  │  ← Repositories + Entities
│  - Database queries (Spring Data)   │
│  - Entity mappings                  │
└─────────────────────────────────────┘
```

**Critical Rules:**
- **Controllers** should NOT contain business logic (only validation & delegation)
- **Services** should NOT access HTTP request/response objects
- **Repositories** should only handle data persistence (no business logic)

---

## Security Architecture

### JWT Authentication Flow

```
1. Registration: POST /api/auth/register
   → AuthController.register()
   → AuthService.register(RegisterRequest)
   → UserService creates User with hashed password (BCrypt)
   → RoleService assigns roles (ADMIN/STORE/CONSUMER)
   → JwtService.generateTokens()
   → Returns: AuthResponse { accessToken, refreshToken }

2. Login: POST /api/auth/login
   → AuthController.login()
   → AuthService.login(LoginRequest)
   → AuthenticationManager validates password
   → JwtService.generateTokens()
   → Returns: AuthResponse { accessToken, refreshToken }

3. Authenticated Request: GET /api/admin/users
   → JwtAuthenticationFilter.doFilterInternal()
   → Extracts "Authorization: Bearer {token}"
   → JwtService.validateToken() + extractUsername()
   → CustomUserDetailsService loads user with roles
   → Sets Spring SecurityContext with authorities (ROLE_ADMIN)
   → SecurityConfig checks: hasRole("ADMIN")
   → AdminController.listUsers() executes

4. Token Refresh: POST /api/auth/refresh
   → AuthController.refresh()
   → AuthService.refresh(RefreshRequest)
   → JwtService validates refreshToken
   → Issues new accessToken (refreshToken remains valid)
   → Returns: AuthResponse { accessToken, refreshToken }

5. OAuth2 Login (Google): GET /oauth2/authorization/google
   → Spring Security redirects to Google
   → User authenticates with Google
   → Callback: /login/oauth2/code/google
   → OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess()
   → Creates or loads User, assigns CONSUMER role (default)
   → JwtService.generateTokens()
   → Redirects with tokens (frontend handles storage)
```

### Role-Based Authorization

**Database Schema:**
```
users (id UUID, email, username, password_hash, first_name, last_name,
       account_expired, account_locked, credentials_expired, enabled,
       created_at, updated_at)
roles (id INTEGER, name)  # Values: "ADMIN", "STORE", "CONSUMER"
user_roles (user_id UUID, role_id INTEGER)  # Many-to-many junction table
```

**Spring Security Convention:**
- **Database stores**: `"ADMIN"` (without prefix)
- **Spring Security expects**: `"ROLE_ADMIN"` (with prefix)
- **Auto-conversion**: Spring adds `ROLE_` prefix when loading authorities
- **In code**: Use `hasRole("ADMIN")` NOT `hasRole("ROLE_ADMIN")`

**SecurityConfig Pattern:**
```kotlin
// Location: src/main/kotlin/lakho/ecommerce/webservices/config/SecurityConfig.kt
.authorizeHttpRequests {
    it
        .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**").permitAll()  // Public
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")           // Admin only
        .requestMatchers("/api/store/**").hasRole("STORE")           // Store only
        .requestMatchers("/api/consumer/**").hasRole("CONSUMER")     // Consumer only
        .anyRequest().authenticated()                                // All others require auth
}
.oauth2Login { oauth2 ->
    oauth2
        .successHandler(oauth2SuccessHandler)
        .failureUrl("/login?error=true")
}
.addFilterBefore(
    JwtAuthenticationFilter(jwtService),
    UsernamePasswordAuthenticationFilter::class.java
)
```

**Security Headers (OWASP Best Practices):**
```kotlin
.headers { headers ->
    headers
        .xssProtection { xss -> xss.headerValue(HeaderValue.ENABLED_MODE_BLOCK) }
        .contentSecurityPolicy { csp -> csp.policyDirectives("default-src 'self'") }
        .frameOptions { frame -> frame.deny() }
        .httpStrictTransportSecurity { hsts ->
            hsts.maxAgeInSeconds(31536000)
                .includeSubDomains(true)
                .preload(true)
        }
}
```

---

## Database Migration Pattern (Liquibase)

### Directory Structure
```
src/main/resources/db/changelog/
│
├── db.changelog-master.yaml          # Entry point (includeAll: /changes)
│
├── changes/                          # Version-level changelogs
│   ├── changelog-v1.0.0.yaml         # Includes feature changelogs from v1.0.0/
│   └── changelog-v1.1.0.yaml         # Includes feature changelogs from v1.1.0/
│
├── v1.0.0/                           # Version 1.0.0 SQL files
│   └── user/                         # Feature: user authentication
│       ├── changelog-user.yaml       # Feature-level changelog
│       ├── create-users-table.sql
│       ├── create-roles-table.sql
│       ├── create-user-roles-table.sql
│       ├── seed-roles.sql
│       └── seed-admin-user.sql
│
└── v1.1.0/                           # Version 1.1.0 SQL files (future)
    └── orders/
        ├── changelog-orders.yaml
        └── create-orders-table.sql
```

### Migration Rules (CRITICAL)

1. **NEVER modify existing SQL files or changesets** - Liquibase tracks by checksum
2. **ChangeSet ID format**: `{feature}-{description}.v{version}` (e.g., `orders-tables.v1`)
3. **Author**: Use consistent identifier (current: "G1")
4. **Always set**: `failOnError: true` and `runOnChange: true`
5. **SQL file references**: Use `relativeToChangelogFile: true`

### Adding New Migration (Step-by-Step)

**Scenario: Add "products" table in version 1.1.0**

```bash
# Step 1: Create directory structure
mkdir -p src/main/resources/db/changelog/v1.1.0/products

# Step 2: Create feature changelog
# File: src/main/resources/db/changelog/v1.1.0/products/changelog-products.yaml
```
```yaml
databaseChangeLog:
  - changeSet:
      id: "products-tables.v1"
      author: "G1"
      failOnError: true
      runOnChange: true
      changes:
        - sqlFile:
            path: create-products-table.sql
            relativeToChangelogFile: true
```

```bash
# Step 3: Create SQL file
# File: src/main/resources/db/changelog/v1.1.0/products/create-products-table.sql
```
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

```bash
# Step 4: Create version changelog (if doesn't exist)
# File: src/main/resources/db/changelog/changes/changelog-v1.1.0.yaml
```
```yaml
databaseChangeLog:
  - include:
      file: ../v1.1.0/products/changelog-products.yaml
```

**Done!** Liquibase will auto-apply on next startup.

---

## Code Generation Guidelines

### Naming Conventions

| Component | Convention | Example |
|-----------|------------|---------|
| Controller | `{Domain}Controller.kt` | `AuthController.kt` |
| Service | `{Domain}Service.kt` | `UserService.kt` |
| Repository | `{Entity}Repository.kt` | `UserRepository.kt` |
| Entity | `{EntityName}.kt` | `User.kt`, `Order.kt` |
| DTO (Request) | `{Action}Request.kt` | `LoginRequest.kt` |
| DTO (Response) | `{Data}Response.kt` or `{Data}.kt` | `AuthResponse.kt`, `UserSummary.kt` |
| Package | lowercase, singular | `auth`, `user`, `admin` |

### Kotlin Best Practices for This Project

**Use data classes for DTOs:**
```kotlin
data class LoginRequest(
    val username: String,
    val password: String
)
```

**Use Spring annotations:**
```kotlin
@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): AuthResponse {
        return authService.login(request)
    }
}
```

**Repository pattern (Spring Data JDBC):**
```kotlin
interface UserRepository : CrudRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
}
```

**Entity with Spring Data JDBC:**
```kotlin
@Table("users")
data class User(
    @Id val id: UUID = UUID.randomUUID(),
    val email: String,
    val username: String,
    val passwordHash: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val accountExpired: Boolean = false,
    val accountLocked: Boolean = false,
    val credentialsExpired: Boolean = false,
    val enabled: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    @Transient val roles: Set<UserRole> = emptySet()
)
```

**Code Quality Standards:**
1. **Unused parameters**: Rename to `_` (underscore) to indicate intentionally unused
   ```kotlin
   // Good
   fun processData(_unusedParam: String, data: Data) {
       // Only use data
   }

   // Bad - unused parameter without underscore
   fun processData(unusedParam: String, data: Data) {
       // Only use data
   }
   ```

2. **Import optimization**: Always use specific imports, never wildcard imports
   ```kotlin
   // Good
   import org.springframework.web.bind.annotation.RestController
   import org.springframework.web.bind.annotation.RequestMapping
   import org.springframework.web.bind.annotation.PostMapping

   // Bad - avoid wildcard imports
   import org.springframework.web.bind.annotation.*
   ```

3. **Remove unused imports**: Clean up imports after refactoring or code changes

**OWASP Security Standards:**

4. **Input Validation** (OWASP A03:2021 - Injection)
   ```kotlin
   // Good - validate and sanitize input
   @PostMapping("/users")
   fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<User> {
       return ResponseEntity.ok(userService.createUser(request))
   }

   data class CreateUserRequest(
       @field:Email(message = "Invalid email format")
       val email: String,

       @field:Size(min = 3, max = 50, message = "Username must be 3-50 characters")
       @field:Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username contains invalid characters")
       val username: String,

       @field:Size(min = 8, message = "Password must be at least 8 characters")
       val password: String
   )

   // Bad - no validation
   fun createUser(email: String, username: String, password: String) {
       userRepository.save(User(email, username, password))
   }
   ```

5. **Parameterized Queries** (OWASP A03:2021 - Injection)
   ```kotlin
   // Good - Spring Data JDBC automatically uses parameterized queries
   interface UserRepository : CrudRepository<User, UUID> {
       @Query("SELECT * FROM users WHERE email = :email")
       fun findByEmail(email: String): User?
   }

   // Bad - NEVER concatenate SQL strings (SQL injection risk)
   // jdbcTemplate.query("SELECT * FROM users WHERE email = '" + email + "'")
   ```

6. **Authentication & Authorization** (OWASP A01:2021 - Broken Access Control)
   ```kotlin
   // Good - explicit role checks
   @GetMapping("/admin/users")
   @PreAuthorize("hasRole('ADMIN')")
   fun getAllUsers(): List<UserSummary> {
       return adminService.getAllUsers()
   }

   // Good - check ownership in service layer
   fun getProfile(userId: UUID, requestingUserId: UUID): Profile {
       if (userId != requestingUserId && !hasAdminRole(requestingUserId)) {
           throw AccessDeniedException("Cannot access other user's profile")
       }
       return profileRepository.findById(userId)
   }
   ```

7. **Sensitive Data Exposure** (OWASP A02:2021 - Cryptographic Failures)
   ```kotlin
   // Good - use DTOs to control exposed fields
   data class UserResponse(
       val id: UUID,
       val email: String,
       val username: String
       // NO passwordHash, no internal flags
   )

   // Bad - exposing entity directly
   // return userRepository.findById(userId) // Exposes passwordHash!
   ```

8. **Secure Password Storage** (OWASP A02:2021 - Cryptographic Failures)
   ```kotlin
   // Good - use BCrypt with proper strength
   @Bean
   fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

   fun registerUser(request: RegisterRequest): User {
       val hashedPassword = passwordEncoder.encode(request.password)
       return userRepository.save(User(passwordHash = hashedPassword, ...))
   }

   // Bad - NEVER store plain text passwords
   ```

9. **Logging Security** (OWASP A09:2021 - Security Logging Failures)
   ```kotlin
   // Good - log security events without sensitive data
   logger.info("Failed login attempt for user: ${request.username}")
   logger.warn("Unauthorized access attempt to: ${request.requestURI}")

   // Bad - NEVER log passwords, tokens, or sensitive data
   // logger.debug("JWT token: $token") // Security risk!
   ```

10. **Error Handling** (OWASP A05:2021 - Security Misconfiguration)
    ```kotlin
    // Good - generic error messages to clients
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuth(ex: AuthenticationException): ResponseEntity<ErrorResponse> {
        logger.error("Authentication failed", ex) // Detailed server-side log
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse("Invalid credentials")) // Generic client message
    }
    ```

11. **JWT Security** (OWASP A07:2021 - Authentication Failures)
    ```kotlin
    // Best practices:
    // - Use strong secret key (min 256 bits)
    // - Set short expiration for access tokens (15 min)
    // - Validate signature, expiration, and claims
    // - Store JWT_SECRET in environment variables only
    // - Use refresh tokens for renewal
    ```

12. **API Security Headers** (OWASP A05:2021 - Security Misconfiguration)
    ```kotlin
    // Add security headers in SecurityConfig
    http.headers { headers ->
        headers
            .xssProtection { it.enable() }
            .frameOptions { it.deny() }
            .httpStrictTransportSecurity { hsts ->
                hsts.maxAgeInSeconds(31536000).includeSubDomains(true)
            }
    }
    ```

13. **Dependency Security** (OWASP A06:2021 - Vulnerable Components)
    ```bash
    # Regularly scan for vulnerable dependencies
    ./gradlew dependencyCheckAnalyze
    # Use tools: OWASP Dependency-Check, Snyk, GitHub Dependabot
    ```

### Security Checklist for New Endpoints

- [ ] Add endpoint to `SecurityConfig.kt` authorization rules
- [ ] Specify required role: `hasRole("ADMIN")` / `hasRole("STORE")` / `hasRole("CONSUMER")`
- [ ] Public endpoints: Add to `.requestMatchers("/api/your/path/**").permitAll()`
- [ ] Test with JWT token in `Authorization: Bearer {token}` header
- [ ] Verify 401 (Unauthorized) without token
- [ ] Verify 403 (Forbidden) with wrong role

---

## Common Tasks - Quick Reference

### Task: Add New Endpoint

**Input**: "Add GET endpoint to list all users (admin only)"

**Output Steps:**
1. Update `AdminController.kt`:
```kotlin
@GetMapping("/users")
fun getAllUsers(): List<UserSummary> {
    return adminService.getAllUsers()
}
```
2. Update `AdminService.kt`:
```kotlin
fun getAllUsers(): List<UserSummary> {
    return userRepository.findAll().map { /* map to UserSummary */ }
}
```
3. Already secured in `SecurityConfig.kt`: `.requestMatchers("/api/admin/**").hasRole("ADMIN")`

### Task: Add Database Field

**Input**: "Add 'phoneNumber' field to User"

**Output Steps:**
1. Create migration SQL:
```sql
-- src/main/resources/db/changelog/v1.1.0/user/add-phone-number-to-users.sql
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);
```
2. Create changeset:
```yaml
# src/main/resources/db/changelog/v1.1.0/user/changelog-user-phone.yaml
databaseChangeLog:
  - changeSet:
      id: "users-add-phone.v1"
      author: "G1"
      failOnError: true
      runOnChange: true
      changes:
        - sqlFile:
            path: add-phone-number-to-users.sql
            relativeToChangelogFile: true
```
3. Update entity:
```kotlin
@Table("users")
data class User(
    @Id val id: UUID = UUID.randomUUID(),
    val email: String,
    val username: String,
    val passwordHash: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,  // Add this
    val accountExpired: Boolean = false,
    val accountLocked: Boolean = false,
    val credentialsExpired: Boolean = false,
    val enabled: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    @Transient val roles: Set<UserRole> = emptySet()
)
```
4. Include in version changelog: Update `changelog-v1.1.0.yaml`

### Task: Add New Role

**Input**: "Add MODERATOR role"

**Output Steps:**
1. Create migration:
```sql
-- src/main/resources/db/changelog/v1.1.0/user/seed-moderator-role.sql
INSERT INTO roles (name) VALUES ('MODERATOR');
```
2. Update `SecurityConfig.kt`:
```kotlin
.requestMatchers("/api/moderator/**").hasRole("MODERATOR")
```
3. Create `ModeratorController.kt` (optional, if new endpoints needed)

---

## Environment Configuration

### Required Environment Variables (.env)

```properties
# Database (PostgreSQL 16)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ecommerce
DB_USER=postgres
DB_PASSWORD=postgres

# Redis 7
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT (CRITICAL - Must be same for all instances, use strong secret in production)
JWT_SECRET=supersecretkeythatshouldbeatleast256bitslong1234567890abcdef
JWT_ACCESS_TOKEN_EXPIRATION_MS=900000         # 15 minutes
JWT_REFRESH_TOKEN_EXPIRATION_MS=604800000     # 7 days

# Application
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

# OpenAPI / Swagger
SPRINGDOC_API_DOCS_PATH=/v3/api-docs
SPRINGDOC_SWAGGER_UI_PATH=/swagger-ui.html

# OAuth2 Google (optional - get from https://console.cloud.google.com)
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret
OAUTH2_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
```

### Running the Application

```bash
# 1. Start infrastructure (PostgreSQL + Redis)
docker-compose up -d

# 2. Verify containers are running
docker-compose ps

# 3. Run application (Liquibase migrations run automatically)
./gradlew bootRun

# 4. Access Swagger UI
# Open browser: http://localhost:8080/swagger-ui.html
```

---

## Troubleshooting Guide

### Issue: 401 Unauthorized

**Possible Causes:**
1. Missing `Authorization` header
2. Token expired (check expiration timestamp)
3. `JWT_SECRET` mismatch between environments
4. Token format incorrect (must be `Bearer {token}`)

**Debug Steps:**
1. Check `JwtAuthenticationFilter.kt` logs
2. Validate token at jwt.io (paste token, verify signature with JWT_SECRET)
3. Check token claims: `exp` (expiration), `sub` (username), `roles`

### Issue: 403 Forbidden

**Possible Causes:**
1. User lacks required role
2. Role stored incorrectly in database (should be "ADMIN" not "ROLE_ADMIN")
3. `SecurityConfig.kt` rule mismatch

**Debug Steps:**
1. Check user's roles: `SELECT * FROM user_roles WHERE user_id = ?`
2. Verify `SecurityConfig.kt` endpoint mapping
3. Check JWT token claims for `roles` array

### Issue: Liquibase Migration Failed

**Possible Causes:**
1. SQL syntax error
2. Duplicate changeSet ID
3. File path incorrect (`relativeToChangelogFile` misconfigured)
4. Database connection issue

**Debug Steps:**
1. Check application startup logs for Liquibase errors
2. Verify SQL syntax in isolation (run in pgAdmin/psql)
3. Check `DATABASECHANGELOG` table: `SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED DESC;`
4. Ensure changeSet IDs are unique across all changelogs

### Issue: Cannot Connect to Database

**Possible Causes:**
1. Docker containers not running
2. Environment variables incorrect
3. PostgreSQL not ready (health check failing)

**Debug Steps:**
```bash
# Check containers
docker-compose ps

# Check PostgreSQL logs
docker-compose logs postgres

# Test connection manually
docker exec -it ecommerce-postgres psql -U postgres -d ecommerce

# Restart containers
docker-compose down
docker-compose up -d
```

---

## API Endpoint Reference

### Public Endpoints (No Auth Required)

```
POST   /api/auth/register                # Register new user with roles
POST   /api/auth/login                   # Login (returns access + refresh tokens)
POST   /api/auth/refresh                 # Refresh access token using refresh token
GET    /oauth2/authorization/google      # Initiate Google OAuth2 login flow
GET    /login/oauth2/code/google         # OAuth2 callback (handled automatically)
GET    /v3/api-docs/**                   # OpenAPI JSON/YAML
GET    /swagger-ui/**                    # Swagger UI
GET    /swagger-ui.html                  # Swagger UI entry point
```

### Protected Endpoints (Require Authentication + Role)

```
# Admin Only
GET    /api/admin/users?page=0&size=10           # List all users (paginated)
GET    /api/admin/users/{id}                     # Get user by UUID
GET    /api/admin/consumers?page=0&size=10       # List consumers (paginated)
GET    /api/admin/stores?page=0&size=10          # List stores (paginated)

# Store Only
GET    /api/store/profile       # Get authenticated store's profile

# Consumer Only
GET    /api/consumer/profile    # Get authenticated consumer's profile
```

### Default Admin Account (Seeded)
- **Email**: admin@ecommerce.com
- **Username**: admin
- **Password**: admin123
- **Role**: ADMIN
- **Location**: `src/main/resources/db/changelog/v1.0.0/user/seed-admin-user.sql`

**Request Format:**
```http
GET /api/admin/users HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Testing Strategy

### Unit Tests (Service Layer)
**Purpose**: Test business logic in isolation
**Location**: `src/test/kotlin/lakho/ecommerce/webservices/{module}/services/`
**Pattern**: `{ServiceName}Test.kt` (e.g., `AuthServiceTest.kt`, `UserServiceTest.kt`)

**Guidelines:**
- Mock all dependencies using Mockito (`mock()`, `when()`, `verify()`)
- Test all public methods in the service
- Cover success cases, error cases, and edge cases
- Use JUnit 5 assertions (`assertEquals`, `assertNotNull`, `assertThrows`)
- Never use real database or external services

**Example Unit Test:**
```kotlin
class AuthServiceTest {
    private lateinit var userService: UserService
    private lateinit var jwtService: JwtService
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        jwtService = mock(JwtService::class.java)
        authService = AuthService(userService, jwtService, ...)
    }

    @Test
    fun `register should create new user successfully`() {
        // Arrange
        val request = RegisterRequest(email = "test@example.com", ...)
        `when`(userService.existsByEmail(request.email)).thenReturn(false)
        `when`(userService.save(any(), any())).thenReturn(savedUser)

        // Act
        val response = authService.register(request)

        // Assert
        assertNotNull(response)
        verify(userService).existsByEmail(request.email)
        verify(userService).save(any(), any())
    }
}
```

### Integration Tests (API Layer)
**Purpose**: Test full API flows with authentication/authorization
**Location**: `src/test/kotlin/lakho/ecommerce/webservices/{module}/api/`
**Pattern**: `{ControllerName}IntegrationTest.kt` (e.g., `AuthControllerIntegrationTest.kt`)

**Guidelines:**
- Use `@SpringBootTest` with `@AutoConfigureMockMvc` and `@Testcontainers`
- Use Testcontainers for PostgreSQL (real database, isolated)
- Test authentication (401 without token, 403 with wrong role)
- Test authorization (verify role-based access control)
- Test request validation (400 for invalid input)
- Use `MockMvc` to perform HTTP requests

**Example Integration Test:**
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
        }
    }

    @Test
    fun `register should create new consumer user successfully`() {
        val request = RegisterRequest(email = "test@example.com", ...)

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.accessToken").exists())
    }

    @Test
    fun `listUsers should return 403 for non-admin user`() {
        mockMvc.perform(
            get("/api/admin/users")
                .header("Authorization", "Bearer $consumerToken")
        )
        .andExpect(status().isForbidden)
    }
}
```

### Test Coverage Requirements
**When creating new features, ALWAYS:**
1. **Unit tests for service layer** - Test all business logic methods
   - Success scenarios
   - Error handling (exceptions, validation failures)
   - Edge cases (null values, empty lists, boundary conditions)

2. **Integration tests for API layer** - Test all endpoints
   - Public endpoints (no authentication required)
   - Protected endpoints with valid authentication
   - Authorization checks (403 for wrong roles)
   - Unauthenticated access (401 without token)
   - Input validation (400 for invalid data)

**Examples of Existing Tests:**
- Unit Tests: `AuthServiceTest.kt`, `UserServiceTest.kt`, `AdminServiceTest.kt`
- Integration Tests: `AuthControllerIntegrationTest.kt`, `AdminControllerIntegrationTest.kt`, `StoreControllerIntegrationTest.kt`, `ConsumerControllerIntegrationTest.kt`

---

## Dependencies Quick Reference

### Core Dependencies (build.gradle.kts)

```kotlin
// Spring Boot Starters
implementation("org.springframework.boot:spring-boot-starter-webmvc")        // REST API
implementation("org.springframework.boot:spring-boot-starter-security")      // Authentication
implementation("org.springframework.boot:spring-boot-starter-oauth2-client") // OAuth2 (Google)
implementation("org.springframework.boot:spring-boot-starter-data-jdbc")     // Database
implementation("org.springframework.boot:spring-boot-starter-liquibase")     // Migrations
implementation("org.springframework.boot:spring-boot-starter-actuator")      // Metrics/Health
implementation("org.springframework.boot:spring-boot-starter-validation")    // Bean Validation
implementation("org.springframework.boot:spring-boot-starter-websocket")     // WebSocket support

// JWT
implementation("io.jsonwebtoken:jjwt-api:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

// OpenAPI / Swagger
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")

// Database
implementation("org.postgresql:postgresql")

// Kotlin
implementation("org.jetbrains.kotlin:kotlin-reflect")
implementation("tools.jackson.module:jackson-module-kotlin")

// Environment & Development
implementation("me.paulschwarz:spring-dotenv:4.0.0")
developmentOnly("org.springframework.boot:spring-boot-devtools")

// Testing
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("org.springframework.boot:spring-boot-starter-security-test")
testImplementation("org.springframework.boot:spring-boot-testcontainers")
testImplementation("org.testcontainers:testcontainers-postgresql")
testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
```

---

## Key Design Decisions

### Why Spring Data JDBC (Not JPA/Hibernate)?
- **Simpler**: No lazy loading, proxy objects, or session management complexity
- **More control**: Explicit queries, predictable behavior
- **Better fit**: For microservices and modular architecture
- **Performance**: Less overhead than JPA/Hibernate

### Why Liquibase (Not Flyway)?
- **Flexibility**: Supports rollbacks, preconditions, and complex changesets
- **Format options**: YAML, XML, SQL, JSON
- **Enterprise features**: Better for large-scale migrations

### Why Modular Monolith (Not Microservices)?
- **Simplicity**: Single deployment, easier debugging
- **Evolution path**: Can split into microservices later (Spring Modulith prepares for this)
- **Team size**: Ideal for small-medium teams
- **Shared data**: Easier to maintain consistency

---

## Response Templates for Claude

### When Explaining Code
**Template:**
```markdown
This code is located in `{file_path}:{line_number}`.

**Purpose**: {Brief description}

**How it works**:
1. {Step 1}
2. {Step 2}

**Related files**: {List related files}
```

### When Adding New Feature
**Template:**
```markdown
I'll add {feature_name} to the {module_name} module.

**Files to create/modify**:
1. `{file_path}` - {Purpose}
2. `{file_path}` - {Purpose}

**Database changes**: {Yes/No - describe if yes}
**Security changes**: {Yes/No - describe if yes}

Let me create these files following the existing patterns...
```

### When Debugging Issue
**Template:**
```markdown
I'll investigate the {issue_description} issue.

**Likely causes**:
1. {Cause 1}
2. {Cause 2}

**Files to check**:
- `{file_path}:{line_number}` - {What to check}

**Diagnostic steps**:
1. {Step 1}
2. {Step 2}

Let me examine the relevant code...
```

---

## Optimization Tips for Claude Agents

### Context Management
1. **Prioritize recent files**: Focus on files in current git status
2. **Module isolation**: Stay within relevant module unless cross-cutting concern
3. **Layer awareness**: Don't mix controller logic in services

### Pattern Recognition
1. **File naming**: If creating new file, match existing patterns exactly
2. **Code style**: Match Kotlin idioms (data classes, null safety, extension functions)
3. **Annotations**: Follow Spring Boot annotation patterns (@RestController, @Service, @Repository)

### Efficiency Strategies
1. **Batch operations**: Create related files in single response (controller + service + repository)
2. **Migration bundling**: Group related schema changes in single changeset
3. **Reference existing**: Point to similar implementations rather than re-explaining

### User Communication
1. **Be concise**: Users prefer short, actionable responses
2. **Use file paths**: Always include `file_path:line_number` references
3. **Show diffs**: When modifying code, show before/after snippets
4. **Explain "why"**: Briefly explain architectural decisions

### Documentation Maintenance
When completing tasks that significantly change the project structure, API endpoints, or architecture:
1. **Update HELP.md**: Add new quick start steps, endpoints, or configuration changes
2. **Update aimodel.md**: Reflect architectural changes, new patterns, or important implementation details
3. **Update CLAUDE.md**: Add new query patterns, response templates, or optimization strategies

**Trigger Events for Documentation Updates:**
- Adding new modules or major features
- Changing authentication/authorization flow
- Adding/modifying API endpoints
- Changing database schema patterns
- Updating technology stack or dependencies
- Adding new development workflows

---

**Version**: 1.0.0
**Last Updated**: 2026-02-14
**Maintained By**: Development Team (G1)
**Optimized For**: Claude AI Agents (Anthropic)
