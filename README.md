# Sprint Reporter — Production-Grade MVP

> Automates Agile sprint reporting by integrating with Jira, allowing Scrum Masters to input remaining story points, compute KPIs, and export clean CSV data for reporting.

---

## Architecture Overview

```
sprint-reporter/
├── backend/          # Spring Boot 3 + Java 17
└── frontend/         # Angular 17 (Standalone API)
```

---

## Backend Architecture

### Layer Diagram

```
HTTP Request
    │
    ▼
┌─────────────────────┐
│   Controller Layer   │  ← Maps HTTP ↔ DTOs, delegates to service
│  IssueController     │
│  ExportController    │
│  GlobalExceptionHdlr │
└────────┬────────────┘
         │ calls
         ▼
┌─────────────────────┐
│   Service Layer      │  ← ALL business logic lives here
│  SprintIssueService  │
│  CsvExportService    │
└────────┬────────────┘
         │ calls (via port interface)
         ▼
┌─────────────────────┐
│   Domain Layer       │  ← Core model, business invariants
│  SprintIssue         │  ← No framework dependency
│  JiraIssueRepo (port)│
│  SpRemainingStore    │
└────────┬────────────┘
         │ implemented by
         ▼
┌─────────────────────┐
│  Infrastructure Lyr  │  ← Jira HTTP client, in-memory store
│  JiraIssueRepoImpl   │
│  MockJiraIssueRepo   │
│  InMemorySpStore     │
│  JiraIssueDomainMap  │
└─────────────────────┘
```

### Package Structure

```
com.company.sprintreporter/
├── SprintReporterApplication.java
├── controller/
│   ├── IssueController.java
│   ├── ExportController.java
│   └── GlobalExceptionHandler.java
├── service/
│   ├── SprintIssueService.java
│   ├── CsvExportService.java
│   └── exception/
│       └── IssueNotFoundException.java
├── domain/
│   ├── model/
│   │   └── SprintIssue.java
│   └── port/
│       ├── JiraIssueRepository.java
│       └── RemainingStoryPointsStore.java
├── application/
│   ├── dto/
│   │   ├── SprintIssueResponseDto.java
│   │   ├── UpdateRemainingSpRequestDto.java
│   │   └── ApiErrorResponseDto.java
│   └── mapper/
│       └── SprintIssueMapper.java (MapStruct)
├── infrastructure/
│   └── jira/
│       ├── JiraProperties.java
│       ├── JiraApiResponse.java
│       ├── JiraIssueRepositoryImpl.java
│       ├── MockJiraIssueRepository.java
│       ├── JiraIssueDomainMapper.java
│       └── InMemoryRemainingStoryPointsStore.java
└── config/
    └── WebConfig.java
```

### REST API Endpoints

| Method | Path                  | Description                             |
|--------|-----------------------|-----------------------------------------|
| GET    | /api/v1/issues        | Get all sprint issues with KPIs         |
| POST   | /api/v1/issues/update | Update remaining SP for an issue        |
| GET    | /api/v1/export/csv    | Download CSV export (file attachment)   |

#### GET /api/v1/issues — Response Example

```json
[
  {
    "issueKey": "SCRUM-101",
    "summary": "Implement user authentication with OAuth2",
    "status": "Done",
    "assignee": "Alice Martin",
    "issueType": "Story",
    "totalStoryPoints": 8,
    "remainingStoryPoints": 0,
    "doneStoryPoints": 8
  },
  {
    "issueKey": "SCRUM-102",
    "summary": "Design sprint dashboard UI mockups",
    "status": "In Progress",
    "assignee": "Bob Chen",
    "issueType": "Story",
    "totalStoryPoints": 5,
    "remainingStoryPoints": 3,
    "doneStoryPoints": 2
  }
]
```

#### POST /api/v1/issues/update — Request Body

```json
{
  "issueKey": "SCRUM-102",
  "remainingStoryPoints": 2
}
```

#### Example Jira API Response (raw)

```json
{
  "issues": [
    {
      "id": "10001",
      "key": "SCRUM-101",
      "fields": {
        "summary": "Implement user authentication",
        "status": { "name": "Done" },
        "assignee": { "displayName": "Alice Martin" },
        "issuetype": { "name": "Story" },
        "customfield_10016": 8.0
      }
    }
  ],
  "total": 1,
  "maxResults": 50,
  "startAt": 0
}
```

---

## Frontend Architecture

### Structure

```
src/app/
├── app.component.ts
├── app.config.ts         (providers, DI)
├── app.routes.ts         (lazy-loaded routes)
├── core/
│   └── interceptors/
│       └── error.interceptor.ts
├── features/
│   └── sprint/
│       ├── sprint.routes.ts
│       ├── models/
│       │   └── sprint-issue.model.ts
│       ├── services/
│       │   ├── sprint-api.service.ts    (HTTP only)
│       │   └── sprint-state.service.ts  (signals-based state)
│       └── components/
│           ├── sprint-dashboard/        (container)
│           ├── sprint-summary-card/     (presentational)
│           ├── sprint-issue-table/      (presentational + local UI state)
│           └── status-badge/            (reusable atom)
└── environments/
    └── environment.ts
```

### Design Principles Applied

| Principle | Implementation |
|-----------|---------------|
| SRP | `SprintApiService` → HTTP only; `SprintStateService` → state only |
| OCP | New status types, new KPIs: no existing code changes needed |
| DIP | Domain ports (interfaces) inverted from infrastructure impls |
| Separation of concerns | Components never call HttpClient; services never render UI |
| Reactivity | Angular Signals for zero-subscription state management |

---

## Running the Application

### Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 20+
- Angular CLI 17+

### Backend

```bash
cd sprint-reporter/backend

# Run with mock Jira data (default — no Jira account needed)
./mvnw spring-boot:run

# Run with real Jira credentials
JIRA_BASE_URL=https://yourcompany.atlassian.net \
JIRA_USER_EMAIL=you@company.com \
JIRA_API_TOKEN=your_api_token \
JIRA_SPRINT_ID=42 \
JIRA_MOCK_MODE=false \
./mvnw spring-boot:run
```

Backend starts at: `http://localhost:8080`

Test the API:
```bash
curl http://localhost:8080/api/v1/issues | jq .
curl -X POST http://localhost:8080/api/v1/issues/update \
  -H "Content-Type: application/json" \
  -d '{"issueKey":"SCRUM-101","remainingStoryPoints":2}'
curl -O -J http://localhost:8080/api/v1/export/csv
```

### Frontend

```bash
cd sprint-reporter/frontend

npm install
npm start
```

Frontend starts at: `http://localhost:4200`
Proxies `/api/*` to `http://localhost:8080` automatically.

---

## Architecture Decisions

### Why Hexagonal Architecture (Ports & Ports)?

The `JiraIssueRepository` interface in the domain layer means:
- The service layer is 100% testable without Jira
- You can swap Jira for Linear, Azure DevOps, or a DB in one file
- Mock mode requires zero changes to any service

### Why In-Memory Store Instead of DB?

The `RemainingStoryPointsStore` port is already defined. To add PostgreSQL:
1. Add Spring Data JPA dependency
2. Create `JpaRemainingStoryPointsStore implements RemainingStoryPointsStore`
3. Annotate with `@Primary`
4. Done. Zero changes to service or controller.

### Why Angular Signals vs NgRx?

For MVP scale, signals provide:
- Computed state (`summary`) that auto-updates
- No boilerplate (no actions/reducers/selectors)
- Full type safety
- Easy migration to NgRx later if needed

### Why MapStruct?

Manual DTO mapping is error-prone and verbose. MapStruct generates
compile-time-safe mappers — if a field is added to the domain model
and not mapped, the build fails.

---

## CI/CD Pipelines

| Workflow | Déclencheur | Description |
|----------|-------------|-------------|
| `backend-ci.yml` | push / PR sur `backend/**` | Lance les tests Maven (`./mvnw test`), upload les rapports Surefire |
| `frontend-ci.yml` | push / PR sur `frontend/**` ou `administration/**` | Lance les tests Angular en headless Chrome |
| `docker-build.yml` | push / PR sur `backend/**` ou `frontend/**` | Vérifie que les images Docker se buildent correctement |
| `dependency-review.yml` | PR uniquement | Bloque les PRs introduisant des dépendances vulnérables (`high`+) |
| `semgrep.yml` | push / PR | Analyse SAST + détection de secrets (Gitleaks) |

Dependabot ouvre automatiquement des PRs chaque semaine pour mettre à jour les dépendances Maven, npm (frontend & administration) et les GitHub Actions.

---

## Playground — Tester une branche sans setup local

Le workflow `playground.yml` se déclenche automatiquement sur les branches `feature/**`, `fix/**` et `chore/**` (ou manuellement depuis l'onglet **Actions** de GitHub).

Il build les images Docker et les publie sur **GHCR** (GitHub Container Registry) avec un tag correspondant au nom de la branche.

### 1. Se connecter au registre (une seule fois)

Génère un **Personal Access Token** GitHub avec le scope `read:packages` :  
`GitHub → Settings → Developer settings → Personal access tokens`

```bash
docker login ghcr.io -u TON_USERNAME --password TON_GITHUB_TOKEN
```

### 2. Lancer l'appli de la branche

Remplace `<owner>` par le nom de ton organisation ou compte GitHub, et `<branch>` par le nom de la branche (les `/` sont remplacés par `-`) :

```bash
BACKEND_IMAGE=ghcr.io/<owner>/sprint-reporter-backend:<branch> \
FRONTEND_IMAGE=ghcr.io/<owner>/sprint-reporter-frontend:<branch> \
docker compose up
```

L'appli démarre sans avoir besoin de Java, Maven ou Node.js installés localement.

> Les commandes exactes avec les bons tags sont aussi affichées dans l'onglet **Summary** du workflow sur GitHub après chaque run.

---

## Extension Roadmap

| Feature | What to add |
|---------|-------------|
| Auth (JWT) | Add `spring-security` + `SecurityConfig`; frontend `AuthInterceptor` |
| Persistent DB | Implement `JpaRemainingStoryPointsStore`; add `@Entity` mapping |
| Multi-sprint support | Add `sprintId` to domain model; extend endpoints |
| Real-time updates | Add WebSocket endpoint; Angular RxJS subscription |
| PDF export | Add `ExportController.exportPdf()` + iText library |
| Role-based access | Spring Security method-level `@PreAuthorize` |
