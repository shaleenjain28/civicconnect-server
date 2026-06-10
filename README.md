# CivicConnect Server (Spring Boot)

Spring Boot backend for the CivicConnect civic issue management platform.  
Migrated from the Express/TypeScript backend.

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4 |
| Language | Java 17 |
| Database | PostgreSQL |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Auth | Supabase JWT + Spring Security |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Build | Maven |

## Package Structure

```
com.civicconnect.server
├── CivicConnectApplication.java    # Entry point
├── config/                         # Spring beans, CORS, app-wide config
├── security/                       # JWT filter, SecurityFilterChain
├── controller/                     # REST controllers (@RestController)
├── service/                        # Business logic (@Service)
├── repository/                     # Data access (@Repository / JpaRepository)
├── entity/                         # JPA entities (@Entity)
│   └── enums/                      # Status, Role, Criticality enums
├── dto/
│   ├── request/                    # Incoming payloads (validated)
│   └── response/                   # Outgoing payloads (shaped)
├── exception/                      # Custom exceptions + @ControllerAdvice
└── util/                           # Helpers (urgency scoring, deadlines, etc.)
```

## Local Setup

```bash
# 1. Copy env file
cp .env.example .env
# Fill in your DB credentials and Supabase keys

# 2. Build
mvn clean install

# 3. Run
mvn spring-boot:run

# 4. API docs
open http://localhost:8080/api/swagger-ui.html
```

## Architecture Layers

```
Request → Controller → Service → Repository → Database
                ↓
              DTO ↔ Entity (mapping happens in Service layer)
```

- **Controller**: Receives HTTP requests, validates input, delegates to Service, returns responses
- **Service**: Contains ALL business logic, orchestrates repositories, maps DTOs ↔ Entities
- **Repository**: Data access only — no business logic, just queries
- **Entity**: Maps to database tables — no business logic
- **DTO**: Shapes data for the API contract — decouples internal model from external API
