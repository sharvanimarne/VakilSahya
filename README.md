# VakilSahay — Plain-Language Legal Document Explainer

> Know exactly what you're signing.

---

## What it does

Upload any legal document (rental agreement, employment contract, loan paper) and get:
- **Clause-by-clause plain English** explanation
- **Risk severity score (0–100)** for every clause (proprietary algorithm)
- **Indian law references** (ICA 1872, MTA 2021, Arbitration Act, etc.)
- **Actionable recommendations** before you sign

---

## Tech Stack

| Layer      | Technology                                    |
|------------|-----------------------------------------------|
| Backend    | Java 17, Spring Boot 3.2, Spring Security + JWT |
| Database   | PostgreSQL + Flyway migrations                |
| ORM        | Spring Data JPA (Hibernate)                   |
| Parsing    | Apache Tika (PDF/DOCX text extraction)        |
| Frontend   | React 18 + Vite + TypeScript + Tailwind CSS   |
| State      | Zustand                                       |
| API Docs   | Swagger/OpenAPI 3.0                           |
| Testing    | JUnit 5 + Mockito (17 tests)                  |

---

## Project Structure

```
vakilsahay/
├── backend/
│   ├── src/main/java/com/vakilsahay/
│   │   ├── config/          — SecurityConfig, SwaggerConfig
│   │   ├── controller/      — AuthController, DocumentController, UserController
│   │   ├── dto/             — Request/Response DTOs
│   │   ├── entity/          — User, Document, Clause, AnalysisReport, UsageLog
│   │   ├── exception/       — GlobalExceptionHandler + custom exceptions
│   │   ├── repository/      — 5 JPA repositories
│   │   ├── security/        — JwtUtil, JwtAuthFilter
│   │   └── service/
│   │       ├── ClauseAnalyzerService.java  ← PATENT CORE ©
│   │       ├── DocumentParserService.java
│   │       ├── DocumentService.java
│   │       ├── ReportGeneratorService.java
│   │       └── AuthService.java
│   └── src/main/resources/
│       ├── application.properties
│       └── db/migration/V1__init_schema.sql
├── frontend/
│   └── src/
│       ├── api/             — Axios client + interceptors
│       ├── pages/           — Landing, Login, Register, Dashboard, Upload, Document
│       ├── components/      — Navbar, SeverityBadge, RiskGauge, etc.
│       ├── store/           — Zustand auth store
│       └── types/           — TypeScript interfaces
└── VakilSahay_Postman_Collection.json
```

---

## Quick Start

### 1. PostgreSQL

```sql
CREATE DATABASE vakilsahay;
```

### 2. Backend

```bash
cd backend
# Edit src/main/resources/application.properties — set DB credentials
mvn spring-boot:run
```

Access:
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/api/swagger-ui.html

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open: http://localhost:5173

### 4. Run JUnit tests

```bash
cd backend
mvn test
```

### 5. Postman

Import `VakilSahay_Postman_Collection.json` into Postman.  
Run TC-01 first (registers user + captures token), then run the full collection.

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/auth/register | Public | Register new user |
| POST | /api/auth/login | Public | Login → JWT token |
| POST | /api/documents/upload | JWT | Upload document |
| GET | /api/documents | JWT | List documents |
| GET | /api/documents/{id} | JWT | Get document + report |
| GET | /api/documents/{id}/clauses | JWT | Get all clauses |
| GET | /api/documents/{id}/clauses?severity=HIGH | JWT | Filter clauses |
| GET | /api/documents/{id}/report | JWT | Full analysis report |
| PUT | /api/documents/{id}/reanalyze | JWT | Re-run analysis |
| DELETE | /api/documents/{id} | JWT | Delete document |
| GET | /api/users/me | JWT | User profile |
| GET | /api/admin/stats | ADMIN | Platform stats |

## Assignment Coverage

| Assignment | Coverage |
|------------|----------|
| 1 — Dev environment | JDK 17, Maven, PostgreSQL, IntelliJ |
| 2 — Spring Boot MVC | Controller → Service → Repository layers |
| 3 — CRUD + REST API | 12 endpoints, PostgreSQL, JPA |
| 3 — JUnit tests | 17 tests in ClauseAnalyzerServiceTest |
| 5 — Postman | 10 test cases in collection JSON |
| 6 — React frontend | React 18 + Vite connected to API |
| 7 — Full integration | All CRUD through UI, success/error states |
