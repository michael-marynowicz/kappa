# 🚀 Sprint Reporter — Backend API

**Agile Sprint Reporting Automation** — Un backend Spring Boot qui s'intègre à Jira pour extraire, calculer et exposer les métriques de sprint (velocity, topic breakdown, capacity, comparaison inter-itérations).

---

## 📐 Architecture

```
Hexagonal / Ports & Adapters
┌────────────────────────────────────────────────────────┐
│  Controller (REST)                                     │
│    IssueController · MetricsController · ExportController │
├────────────────────────────────────────────────────────┤
│  Service (Business Logic)                              │
│    SprintIssueService · MetricsService · CsvExportService │
├────────────────────────────────────────────────────────┤
│  Domain (Models + Ports)                               │
│    SprintIssue · IterationSnapshot                     │
│    JiraIssueRepository (port) · RemainingStoryPointsStore (port) │
├────────────────────────────────────────────────────────┤
│  Infrastructure (Adapters)                             │
│    JiraIssueRepositoryImpl · MockJiraIssueRepository   │
│    InMemoryRemainingStoryPointsStore                   │
└────────────────────────────────────────────────────────┘
```

**Principes :** DDD, Clean Architecture, Port/Adapter pattern, MapStruct pour le mapping DTO.

---

## 🔧 Prérequis

| Outil  | Version |
|--------|---------|
| Java   | 17+     |
| Maven  | 3.9+    |
| Docker | 24+ *(optionnel)* |

---

## ⚡ Démarrage rapide

### Mode mock (sans Jira)

```bash
mvn spring-boot:run
```

Le mode mock est activé par défaut (`jira.mock-mode=true`). L'API démarre sur `http://localhost:8080` avec des données de démo.

### Mode réel (avec Jira)

1. Créer un fichier `src/main/resources/application-local.yml` :

```yaml
jira:
  base-url: https://your-jira-instance.com/agile
  user-email: your.email
  pat: "YOUR_PERSONAL_ACCESS_TOKEN"
  project-key: YOUR_PROJECT
  board-id: 12345
  mock-mode: false

metrics:
  capacity:
    planned: 120
    real: 105
  team-availability:
    dev: 5.5
    pda: 2.0
    qa: 3.0
```

2. Lancer avec le profil local :

```bash
# 1. Lancer PostgreSQL
docker-compose up -d

# Linux / macOS / Git Bash
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 2. Lancer l'app avec profil local (PowerShell)
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

### Docker

```bash
# Build
docker build -t sprint-reporter .

# Run
docker run -p 8080:8080 \
  -e JIRA_BASE_URL=https://your-jira.com \
  -e JIRA_PAT=your-token \
  -e JIRA_PROJECT_KEY=PROJ \
  -e JIRA_MOCK_MODE=false \
  sprint-reporter
```

---

## 📡 API Endpoints

### Issues

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET`   | `/api/v1/issues` | Liste toutes les issues du sprint actif avec KPIs calculés |
| `POST`  | `/api/v1/issues/update` | Met à jour les remaining story points d'une issue |

#### `POST /api/v1/issues/update` — Body

```json
{
  "issueKey": "ROC-42",
  "remainingStoryPoints": 3
}
```

### Métriques

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET`   | `/api/v1/metrics` | Velocity, topic breakdown, capacity, team availability |
| `GET`   | `/api/v1/metrics/iterations` | Comparaison des itérations passées + courante |

#### `GET /api/v1/metrics` — Réponse

```json
{
  "committedStoryPoints": 60,
  "deliveredStoryPoints": 45,
  "workStoryPoints": 45,
  "leftoverStoryPoints": 15,
  "ratio": 75.0,
  "topicBreakdown": [
    { "topic": "Backend", "storyPoints": 24 },
    { "topic": "Security", "storyPoints": 8 },
    { "topic": "Frontend", "storyPoints": 5 }
  ],
  "capacity": {
    "plannedCapacity": 120.0,
    "realCapacity": 105.0
  },
  "teamAvailability": {
    "dev": 5.5,
    "pda": 2.0,
    "qa": 3.0
  }
}
```

#### `GET /api/v1/metrics/iterations` — Réponse

```json
[
  {
    "sprintName": "Sprint 10",
    "committedStoryPoints": 21,
    "deliveredStoryPoints": 16,
    "velocity": 16,
    "ratio": 76.2
  },
  {
    "sprintName": "Sprint 11",
    "committedStoryPoints": 34,
    "deliveredStoryPoints": 26,
    "velocity": 26,
    "ratio": 76.5
  },
  {
    "sprintName": "Current Sprint",
    "committedStoryPoints": 60,
    "deliveredStoryPoints": 45,
    "velocity": 45,
    "ratio": 75.0
  }
]
```

### Export

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET`   | `/api/v1/export/csv` | Télécharge le rapport sprint au format CSV |

---

## ⚙️ Configuration

Toutes les propriétés sont configurables via `application.yml`, profils Spring ou variables d'environnement.

### Jira

| Propriété | Env var | Défaut | Description |
|-----------|---------|--------|-------------|
| `jira.base-url` | `JIRA_BASE_URL` | `https://your-company.atlassian.net` | URL de base Jira |
| `jira.pat` | `JIRA_PAT` | *(vide)* | Personal Access Token (Jira Server 8.14+) |
| `jira.api-token` | `JIRA_API_TOKEN` | `mock-token` | API token (Jira Cloud) |
| `jira.user-email` | `JIRA_USER_EMAIL` | `user@company.com` | Email utilisateur |
| `jira.project-key` | `JIRA_PROJECT_KEY` | `SCRUM` | Clé du projet Jira |
| `jira.board-id` | — | — | ID du board Scrum |
| `jira.sprint-id` | `JIRA_SPRINT_ID` | `42` | ID du sprint (fallback) |
| `jira.mock-mode` | `JIRA_MOCK_MODE` | `true` | Active le mode données fictives |

### Métriques

| Propriété | Env var | Défaut | Description |
|-----------|---------|--------|-------------|
| `metrics.capacity.planned` | `METRICS_CAPACITY_PLANNED` | `0` | Capacité planifiée (SP) |
| `metrics.capacity.real` | `METRICS_CAPACITY_REAL` | `0` | Capacité réelle (SP) |
| `metrics.team-availability.dev` | `METRICS_TEAM_DEV` | `0` | EFT développeurs |
| `metrics.team-availability.pda` | `METRICS_TEAM_PDA` | `0` | EFT PDA |
| `metrics.team-availability.qa` | `METRICS_TEAM_QA` | `0` | EFT QA |

---

## 🔐 Authentification Jira

Trois modes supportés (auto-détection) :

| Mode | Condition | Header |
|------|-----------|--------|
| **PAT** | `jira.pat` renseigné | `Authorization: Bearer <pat>` |
| **Cookie** | `jira.api-token` contient `JSESSIONID=` | `Cookie: JSESSIONID=...` |
| **Basic** | Sinon | `Authorization: Basic <base64(email:token)>` |

> **Recommandé :** Utiliser un PAT (Personal Access Token). Générer dans Jira → Profil → Personal Access Tokens.

---

## 🧪 Tests

```bash
# Lancer tous les tests
mvn test

# Lancer un test spécifique
mvn test -Dtest=SprintIssueServiceTest

# Avec couverture (si JaCoCo configuré)
mvn verify
```

**Stack de test :** JUnit 5, Mockito, AssertJ, Spring MockMvc.

---

## 📂 Structure du projet

```
src/main/java/com/company/sprintreporter/
├── application/
│   ├── dto/                          # DTOs de réponse/requête
│   │   ├── SprintIssueResponseDto
│   │   ├── SprintMetricsResponseDto
│   │   ├── IterationSnapshotDto
│   │   ├── TopicBreakdownDto
│   │   ├── CapacityDto
│   │   ├── TeamAvailabilityDto
│   │   ├── UpdateRemainingSpRequestDto
│   │   └── ApiErrorResponseDto
│   └── mapper/
│       └── SprintIssueMapper         # MapStruct: domain → DTO
├── config/
│   ├── WebConfig                     # CORS
│   └── MetricsProperties             # Config capacity & team
├── controller/
│   ├── IssueController               # /api/v1/issues
│   ├── MetricsController             # /api/v1/metrics
│   ├── ExportController              # /api/v1/export
│   └── GlobalExceptionHandler        # Gestion centralisée des erreurs
├── domain/
│   ├── model/
│   │   ├── SprintIssue               # Entité cœur (logique métier ici)
│   │   └── IterationSnapshot         # Snapshot d'itération
│   └── port/
│       ├── JiraIssueRepository       # Port sortant (interface)
│       └── RemainingStoryPointsStore  # Port sortant (interface)
├── service/
│   ├── SprintIssueService            # Orchestration issues
│   ├── MetricsService                # Calcul des métriques
│   ├── CsvExportService              # Export CSV
│   └── exception/
│       └── IssueNotFoundException
├── infrastructure/jira/
│   ├── JiraIssueRepositoryImpl       # Adapter réel (API Jira)
│   ├── MockJiraIssueRepository       # Adapter mock (dev/demo)
│   ├── JiraIssueDomainMapper         # Anti-corruption layer
│   ├── JiraApiResponse               # Modèles réponse Jira
│   ├── JiraProperties                # Config Jira
│   └── InMemoryRemainingStoryPointsStore
└── SprintReporterApplication         # Point d'entrée
```

---

## 🐳 Docker

Le Dockerfile utilise un **multi-stage build** :
1. **Build** : `eclipse-temurin:17-jdk-alpine` — compile le JAR
2. **Runtime** : `eclipse-temurin:17-jre-alpine` — image minimale (~150 MB)

Sécurité : l'application tourne sous un utilisateur non-root (`appuser`).

---

## 📊 Métriques calculées

| Métrique | Calcul |
|----------|--------|
| **Committed** | Σ totalStoryPoints de toutes les issues |
| **Delivered** | Σ doneStoryPoints (issues terminées) |
| **Work** | Committed − Leftover |
| **Leftover** | Σ (total − done) pour les issues non terminées |
| **Ratio** | (Delivered / Committed) × 100 |
| **Velocity** | = Delivered (par sprint) |
| **Topic Breakdown** | SP groupés par label Jira (1er label = topic) |

---

## 🤝 Frontend

Le backend est conçu pour fonctionner avec un frontend Angular servi sur `http://localhost:4200` (CORS pré-configuré).

Les graphes attendus côté front :
- **Velocity** : bar chart (committed, delivered, work, leftover, ratio)
- **Topics** : pie chart / camembert (SP par topic)
- **Capacity** : planned vs real
- **Team Availability** : EFT split dev / PDA / QA
- **Iterations** : bar chart comparatif des sprints passés
