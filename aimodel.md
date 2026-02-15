 # AI Model Context Guide - E-Commerce Webservices

## Project Summary

This is a **Spring Boot 4.0.2** microservices application written in **Kotlin 2.2.21** for an e-commerce platform. The system implements JWT-based authentication with role-based authorization (RBAC) supporting three user types: **Admin**, **Store**, and **Consumer**.

### Technology Stack
- **Framework**: Spring Boot 4.0.2, Spring Security, Spring Data JDBC
- **Language**: Kotlin 2.2.21 with Java 17
- **Database**: PostgreSQL (via JDBC)
- **Caching**: Redis
- **Build Tool**: Gradle 9.3.0 with Kotlin DSL
- **Migration**: Liquibase (DB schema versioning)
- **Authentication**: JWT (JSON Web Tokens) with BCrypt password hashing
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Environment**: Docker Compose for PostgreSQL & Redis
- **Module System**: Spring Modulith (modular monolith architecture)

---

## Architecture Overview

### Architectural Pattern
**Modular Monolith** using Spring Modulith - A single deployable unit organized into loosely-coupled modules with clear boundaries.

### Module Structure
The application is organized into domain-driven modules:

```
lakho.ecommerce.webservices/
├── auth/           # Authentication & JWT management
├── user/           # User domain (entities, repositories)
├── admin/          # Admin operations
├── storeowner/     # Store owner management
├── consumer/       # Consumer operations
└── config/         # Cross-cutting configurations (Security, OpenAPI, JWT filters)
```

### Layered Architecture Pattern
Each module follows a **3-layer architecture**:

```
api/                  # Controllers & DTOs (presentation layer)
├── XxxController.kt
└── models/           # Request/Response DTOs

services/             # Business logic (service layer)
└── XxxService.kt

repositories/         # Data access (persistence layer)
├── entities/         # Database entities
└── XxxRepository.kt
```

**Example**: The `user` module structure:
```
user/
├── package-info.java            # Spring Modulith module definition
├── repositories/
│   ├── entities/
│   │   ├── User.kt
│   │   ├── Role.kt
│   │   └── UserRole.kt
│   ├── UserRepository.kt
│   ├── RoleRepository.kt
│   └── UserRoleRepository.kt
└── services/
    └── UserService.kt
```

### Security Architecture
- **Stateless sessions** - No server-side session storage
- **JWT-based authentication** - Access tokens (15 min) + Refresh tokens (7 days)
- **Role-based authorization** - Three roles: ADMIN, STORE_OWNER, CONSUMER
- **BCrypt password encryption**
- **Custom JWT filter** (`JwtAuthenticationFilter`) runs before Spring Security's authentication filter

### API Structure
All endpoints follow RESTful conventions:

```
/api/auth/**        - Public (registration, login, token refresh)
/api/admin/**       - ADMIN role only
/api/storeowner/**  - STORE_OWNER role only
/api/consumer/**    - CONSUMER role only
/v3/api-docs/**     - OpenAPI documentation (public)
/swagger-ui/**      - Swagger UI (public)
```

### Current API Endpoints

**Authentication (Public):**
- `POST /api/auth/register` - Register new user with roles
- `POST /api/auth/login` - Login and receive JWT tokens (accessToken + refreshToken)
- `POST /api/auth/refresh` - Refresh access token using refresh token

**Admin Endpoints (ADMIN role required):**
- `GET /api/admin/users?page=0&size=10` - List all users (paginated)
- `GET /api/admin/users/{id}` - Get specific user by UUID
- `GET /api/admin/consumers?page=0&size=10` - List all consumers (paginated)
- `GET /api/admin/stores?page=0&size=10` - List all stores (paginated)

**Store Owner Endpoints (STORE_OWNER role required):**
- `GET /api/storeowner/profile` - Get authenticated store owner's profile

**Consumer Endpoints (CONSUMER role required):**
- `GET /api/consumer/profile` - Get authenticated consumer's profile

---

## Database Migration Pattern

### Liquibase Configuration
- **Migration Tool**: Liquibase (preferred over Flyway)
- **Master Changelog**: `src/main/resources/db/changelog/db.changelog-master.yaml`
- **Pattern**: Version-based folder structure with feature-specific sub-folders

### Directory Structure
```
src/main/resources/db/
└── changelog/
    ├── db.changelog-master.yaml      # Master file (includes all changes)
    ├── changes/
    │   └── changelog-v1.0.0.yaml     # Version-specific changelog
    └── v1.0.0/
        └── user/
            ├── changelog-user.yaml        # Feature-specific changelog
            ├── create-users-table.sql
            ├── create-roles-table.sql
            ├── create-user-roles-table.sql
            ├── seed-roles.sql
            └── seed-admin-user.sql
```

### Migration Pattern Rules

1. **Master Changelog Strategy**:
   - `db.changelog-master.yaml` uses `includeAll` to include all version changelogs from `/changes`
   - Never directly add changesets to master file

2. **Version Changelogs** (`changelog-v1.0.0.yaml`):
   - Each version has a single changelog file in `/changes/` folder
   - Includes feature-specific changelogs from versioned folders

3. **Feature Changelogs** (`changelog-user.yaml`):
   - Group related database changes by feature/domain
   - Each changeset has unique ID: `{feature}-{description}.v{version}` (e.g., `authentications-tables.v1`)
   - Use `sqlFile` references with `relativeToChangelogFile: true`

4. **SQL Files**:
   - Store actual SQL in separate `.sql` files
   - Keep files focused (one table per file for creation)
   - Include seed data files for reference data

### Adding New Migrations

**For a new feature in existing version:**
```yaml
# In v1.0.0/myfeature/changelog-myfeature.yaml
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

**For a new version:**
1. Create `v1.1.0/` folder
2. Create `changelog-v1.1.0.yaml` in `/changes/`
3. Reference feature changelogs from versioned folder

### Configuration
```properties
spring.liquibase.enabled=true
spring.liquibase.show-summary=summary
spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.yaml
```

---

## Folder & Package Structure

### Root Project Structure
```
webservices/
├── .env.example                    # Environment variables template
├── docker-compose.yml              # PostgreSQL + Redis containers
├── build.gradle.kts                # Gradle build configuration
├── settings.gradle.kts
├── gradlew / gradlew.bat           # Gradle wrapper scripts
├── HELP.md
├── src/
│   ├── main/
│   │   ├── kotlin/                 # Kotlin source code
│   │   └── resources/              # Configuration files
│   └── test/
│       └── kotlin/                 # Test code
└── build/                          # Build outputs (ignore)
```

### Source Code Structure
```
src/main/kotlin/lakho/ecommerce/webservices/
├── WebservicesApplication.kt       # Main application entry point
│
├── auth/                           # Authentication module
│   ├── JwtProperties.kt            # JWT configuration properties
│   ├── api/
│   │   ├── AuthController.kt
│   │   └── models/
│   │       ├── LoginRequest.kt
│   │       ├── RegisterRequest.kt
│   │       ├── RefreshRequest.kt
│   │       └── AuthResponse.kt
│   └── services/
│       ├── AuthService.kt
│       └── JwtService.kt
│
├── user/                           # User domain module
│   ├── package-info.java           # Spring Modulith module definition
│   ├── repositories/
│   │   ├── entities/
│   │   │   ├── User.kt
│   │   │   ├── Role.kt
│   │   │   └── UserRole.kt
│   │   ├── UserRepository.kt
│   │   ├── RoleRepository.kt
│   │   └── UserRoleRepository.kt
│   └── services/
│       └── UserService.kt
│
├── admin/                          # Admin module
│   ├── api/
│   │   ├── AdminController.kt
│   │   └── models/
│   │       └── UserSummary.kt
│   └── services/
│       └── AdminService.kt
│
├── storeowner/                     # Store owner module
│   ├── api/
│   │   ├── StoreController.kt
│   │   └── models/
│   │       └── StoreProfile.kt
│   └── services/
│       └── StoreService.kt
│
├── consumer/                       # Consumer module
│   ├── api/
│   │   ├── ConsumerController.kt
│   │   └── models/
│   │       └── ConsumerProfile.kt
│   └── services/
│       └── ConsumerService.kt
│
└── config/                         # Cross-cutting configurations
    ├── SecurityConfig.kt           # Spring Security configuration
    ├── JwtAuthenticationFilter.kt  # JWT filter
    └── OpenApiConfiguration.kt     # Swagger configuration
```

### Resources Structure
```
src/main/resources/
├── application.properties          # Spring Boot configuration
└── db/
    └── changelog/
        ├── db.changelog-master.yaml
        ├── changes/                # Version changelogs
        └── v1.0.0/                # SQL migration files
            └── user/
```

### Naming Conventions

**Packages**: lowercase, domain-driven (auth, user, admin, store, consumer)

**Files**:
- Controllers: `{Domain}Controller.kt` (e.g., `AuthController.kt`)
- Services: `{Domain}Service.kt` (e.g., `UserService.kt`)
- Repositories: `{Entity}Repository.kt` (e.g., `UserRepository.kt`)
- Entities: `{EntityName}.kt` (e.g., `User.kt`, `Role.kt`)
- DTOs: Descriptive names in `/models/` (e.g., `LoginRequest.kt`, `AuthResponse.kt`)

**Module Organization**:
- Each module has `package-info.java` for Spring Modulith metadata
- Repositories and entities are internal to modules
- API layer exposes public interfaces
- Services contain business logic

---

## Key Configuration Files

### Environment Variables (`.env`)
```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ecommerce
DB_USER=postgres
DB_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=supersecretkeythatshouldbeatleast256bitslong1234567890abcdef
JWT_ACCESS_TOKEN_EXPIRATION_MS=900000         # 15 minutes
JWT_REFRESH_TOKEN_EXPIRATION_MS=604800000     # 7 days

# Application
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

# OpenAPI
SPRINGDOC_API_DOCS_PATH=/v3/api-docs
SPRINGDOC_SWAGGER_UI_PATH=/swagger-ui.html
```

### Application Properties (`application.properties`)
- Uses environment variables with sensible defaults
- Pattern: `${VARIABLE_NAME:default_value}`
- Critical settings: datasource, Liquibase, Redis, JWT, OpenAPI

### Docker Compose (`docker-compose.yml`)
Provides local development infrastructure:
- **PostgreSQL 16** on port 5432 with health checks
- **Redis 7** on port 6379 with persistence
- Network: `ecommerce-network`
- Volumes: `postgres_data`, `redis_data`

---

## Common Development Tasks

### Running the Application
```bash
# Start infrastructure
docker-compose up -d

# Run application (from project root)
./gradlew bootRun

# Or build and run JAR
./gradlew build
java -jar build/libs/webservices-0.0.1-SNAPSHOT.jar
```

### Database Migrations
```bash
# Liquibase runs automatically on startup
# To manually validate:
./gradlew liquibaseValidate

# To rollback (if configured):
./gradlew liquibaseRollback
```

### API Documentation
Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport
```

---

## Important Implementation Details

### JWT Authentication Flow
1. User registers via `/api/auth/register` → User created with role(s)
2. User logs in via `/api/auth/login` → Returns `accessToken` + `refreshToken`
3. Client includes token in requests: `Authorization: Bearer {accessToken}`
4. `JwtAuthenticationFilter` validates token and sets Spring Security context
5. Token expires → Client uses `/api/auth/refresh` with `refreshToken`

### User Roles System
- **Database**: Many-to-many relationship (users ↔ user_roles ↔ roles)
- **Roles**: ADMIN, STORE_OWNER, CONSUMER (stored without `ROLE_` prefix)
- **Spring Security**: Automatically adds `ROLE_` prefix
- **Authorization**: `@PreAuthorize` or `hasRole()` in SecurityConfig
- **User Entity**: UUID primary key, includes firstName, lastName, account status flags (expired, locked, credentialsExpired, enabled)

### Security Configuration
- CSRF disabled (stateless JWT)
- Session management: STATELESS
- Public endpoints: `/api/auth/**`, Swagger docs
- Protected endpoints: Require authentication + role
- Password encoding: BCrypt with strength 10 (default)

### Spring Modulith
- Modules defined via `@ApplicationModule` in `package-info.java`
- Type `OPEN` allows other modules to access public APIs
- Enables modular testing and event-driven communication

---

## AI Assistant Guidelines

### When Understanding User Queries

1. **Module Context**: Determine which module the query relates to (auth, user, admin, store, consumer)
2. **Layer Context**: Identify the layer (API/controller, service, repository)
3. **Security Context**: Check if the task involves authentication/authorization
4. **Migration Context**: Recognize database-related changes requiring Liquibase migrations

### Common Query Patterns

**"Add a new endpoint"**
→ Check module → Create/update Controller in `api/` → Add method to Service → Update SecurityConfig if needed

**"Add a field to User"**
→ Update `User.kt` entity → Create Liquibase migration SQL → Test repository

**"Fix authentication issue"**
→ Check `JwtAuthenticationFilter.kt`, `JwtService.kt`, `AuthService.kt`, `SecurityConfig.kt`

**"Add a new role"**
→ Create migration to insert into `roles` table → Update `SecurityConfig.kt` authorization rules

**"Change database schema"**
→ **Development**: Modify existing `create-{table}-table.sql` directly if table already exists
→ **Production**: Create new changeset with ALTER TABLE migration
→ Update entity if needed

### Code Generation Best Practices

1. **Follow existing patterns**: Match naming conventions and file organization
2. **Maintain layer separation**: Don't mix controller logic in services
3. **Use Kotlin idioms**: Data classes, null safety, extension functions
4. **Security first**: Always consider authorization for new endpoints
5. **Database changes (Development)**: Modify existing `create-{table}-table.sql` files directly instead of creating ALTER TABLE migrations
6. **Database changes (Production)**: Use ALTER TABLE migrations in new changesets for tables with existing data
7. **Testing**: Follow Spring Boot test patterns (use Testcontainers for integration tests)

### File Path Templates

- Controller: `src/main/kotlin/lakho/ecommerce/webservices/{module}/api/{Module}Controller.kt`
- Service: `src/main/kotlin/lakho/ecommerce/webservices/{module}/services/{Module}Service.kt`
- Repository: `src/main/kotlin/lakho/ecommerce/webservices/{module}/repositories/{Entity}Repository.kt`
- Entity: `src/main/kotlin/lakho/ecommerce/webservices/{module}/repositories/entities/{Entity}.kt`
- DTO: `src/main/kotlin/lakho/ecommerce/webservices/{module}/api/models/{Name}.kt`
- Migration: `src/main/resources/db/changelog/v{version}/{feature}/{description}.sql`

---

## Dependencies Reference

### Core Spring Boot Starters
- `spring-boot-starter-actuator` - Health checks & metrics
- `spring-boot-starter-data-jdbc` - Database access (not JPA!)
- `spring-boot-starter-data-redis` - Redis caching
- `spring-boot-starter-security` - Authentication & authorization
- `spring-boot-starter-webmvc` - REST API
- `spring-boot-starter-websocket` - WebSocket support

### Key Libraries
- `io.jsonwebtoken:jjwt-*:0.12.6` - JWT creation & validation
- `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6` - OpenAPI/Swagger
- `me.paulschwarz:spring-dotenv:4.0.0` - .env file support
- `org.springframework.boot:spring-boot-starter-liquibase` - Database migrations
- `org.postgresql:postgresql` - PostgreSQL JDBC driver

### Build Plugins
- Kotlin JVM plugin 2.2.21
- Spring Boot plugin 4.0.2
- Spring Dependency Management 1.1.7
- AsciiDoctor 4.0.5 (for REST Docs)

---

## Troubleshooting Common Issues

### Migration Failures
- **Issue**: Liquibase fails to run
- **Check**: Changelog paths are correct, `relativeToChangelogFile: true` is set
- **Fix**: Verify SQL syntax, ensure changeSet IDs are unique

### JWT Authentication Not Working
- **Issue**: 401 Unauthorized despite valid token
- **Check**: `JWT_SECRET` matches between token creation and validation
- **Check**: Token not expired, role matches endpoint requirements
- **Fix**: Verify `JwtAuthenticationFilter` is registered before `UsernamePasswordAuthenticationFilter`

### Role-Based Access Denied
- **Issue**: 403 Forbidden despite user having role
- **Check**: Role stored in DB without `ROLE_` prefix
- **Check**: SecurityConfig uses `hasRole("ADMIN")` not `hasRole("ROLE_ADMIN")`
- **Fix**: Ensure `UserRole` entity maps correctly to Spring Security authorities

### Database Connection Issues
- **Issue**: Cannot connect to PostgreSQL
- **Check**: Docker containers running (`docker-compose ps`)
- **Check**: Environment variables match `.env` file
- **Fix**: Restart containers, verify network configuration

---

## Version History

- **v1.0.0**: Initial release
  - User authentication with JWT (access + refresh tokens)
  - Role-based authorization (ADMIN, STORE_OWNER, CONSUMER)
  - User management with UUID primary keys
  - PostgreSQL database with Liquibase migrations
  - Redis caching setup
  - OpenAPI/Swagger documentation with JWT authentication
  - Admin endpoints for user/consumer/store listing (paginated)
  - Store and Consumer profile endpoints
  - Default admin user seeded (admin@ecommerce.com / admin123)

---

## Additional Notes

- **No JPA/Hibernate**: Using Spring Data JDBC (simpler, more control)
- **Kotlin-first**: Leverage Kotlin features (data classes, null safety, etc.)
- **Modular design**: Spring Modulith enables gradual transition to microservices if needed
- **Security**: Stateless JWT approach suitable for microservices/distributed systems
- **DevOps**: Docker Compose for local development; ready for Kubernetes deployment

---

**Last Updated**: 2026-02-14
**Maintained By**: Development Team (G1)
