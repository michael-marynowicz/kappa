# Sprint Reporter — Frontend

Angular 17 single-page application for agile sprint reporting automation.  
Connects to the Sprint Reporter backend API to visualize sprint data, track story points, and compare iterations.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Framework    | Angular 17.3 (standalone components)|
| Language     | TypeScript 5.4                      |
| Styling      | SCSS + CSS custom properties        |
| HTTP         | Angular HttpClient + RxJS 7.8       |
| State        | Angular Signals                     |
| Build        | Angular CLI / esbuild               |
| Tests        | Jasmine + Karma                     |
| Production   | Docker (nginx 1.25-alpine)          |

---

## Project Structure

```
src/
├── main.ts                          # Bootstrap
├── index.html
├── styles.scss                      # Global styles & design tokens
├── environments/
│   ├── environment.ts               # Dev (proxy to localhost:8080)
│   └── environment.prod.ts          # Production
└── app/
    ├── app.component.ts             # Root component
    ├── app.config.ts                # App providers
    ├── app.routes.ts                # Top-level routes (lazy-loaded)
    ├── core/
    │   └── interceptors/
    │       └── error.interceptor.ts  # Global HTTP error handling
    └── features/
        └── sprint/
            ├── sprint.routes.ts      # Feature routes
            ├── models/
            │   └── sprint-issue.model.ts   # Domain interfaces
            ├── services/
            │   ├── sprint-api.service.ts    # HTTP client
            │   └── sprint-state.service.ts  # Signal-based state
            └── components/
                ├── sprint-dashboard/       # Main page (tab bar)
                ├── sprint-summary-card/    # KPI cards
                ├── sprint-issue-table/     # US/issues board
                ├── sprint-analytics/       # Metrics charts
                └── status-badge/           # Status pill
```

---

## Features

### Board Tab
- Sprint issue table with status badges, assignee, story points
- Inline remaining SP editing with optimistic update
- CSV export

### Metrics Tab
- **Velocity** — Committed / Delivered / Work / Leftover bars + ratio
- **Story Points by Topic** — Donut chart with per-epic breakdown
- **Capacity & Team Availability** — Planned vs Real capacity bars + EFT split (DEV / PDA / QA)
- **Iteration Comparison** — Cross-sprint table with stacked committed/delivered bars

### Shared
- KPI summary cards (Total Issues, Total SP, Done SP, Remaining SP, Completion %)
- Global error banner with dismissal
- Responsive layout (mobile-friendly)

---

## Getting Started

### Prerequisites

- **Node.js** ≥ 20
- **npm** ≥ 10
- Backend API running on `http://localhost:8080` (or update `proxy.conf.json`)

### Install & Run

```bash
# Install dependencies
npm ci

# Start dev server (proxies /api to localhost:8080)
npm start
```

App available at **http://localhost:4200**

### Build for Production

```bash
npm run build:prod
```

Output: `dist/sprint-reporter-ui/browser/`

---

## Docker

```bash
# Build image
docker build -t sprint-reporter-ui .

# Run container
docker run -p 80:80 sprint-reporter-ui
```

The nginx config handles:
- SPA routing (`try_files` → `index.html`)
- API proxy to `http://backend:8080`
- Gzip compression
- Aggressive static asset caching (1 year, immutable)

---

## Available Scripts

| Script            | Description                                |
|-------------------|--------------------------------------------|
| `npm start`       | Dev server with API proxy (port 4200)      |
| `npm run build`   | Development build                          |
| `npm run build:prod` | Production build with optimizations     |
| `npm run watch`   | Rebuild on changes (dev)                   |
| `npm test`        | Run unit tests (Karma + Jasmine)           |
| `npm run lint`    | Lint the project                           |

---

## API Endpoints Consumed

| Method | Endpoint                      | Description                    |
|--------|-------------------------------|--------------------------------|
| GET    | `/api/v1/issues`              | Sprint issues list             |
| POST   | `/api/v1/issues/update`       | Update remaining SP            |
| GET    | `/api/v1/metrics`             | Sprint metrics & KPIs          |
| GET    | `/api/v1/metrics/iterations`  | Iteration history (comparison) |
| GET    | `/api/v1/export/csv`          | CSV report download            |

---

## Environment Configuration

| Variable     | Dev                | Prod                                          |
|--------------|--------------------|------------------------------------------------|
| `apiBaseUrl` | `""` (uses proxy)  | `https://api.sprint-reporter.yourcompany.com`  |
| `production` | `false`            | `true`                                         |

Override production API URL at build time or in `environment.prod.ts`.

---

## Design System

Dark theme with amber accent — CSS custom properties defined in `styles.scss`:

| Token                    | Value            |
|--------------------------|------------------|
| `--color-bg`             | `#0d0f14`        |
| `--color-surface`        | `#161921`        |
| `--color-accent`         | `#f59e0b` (amber)|
| `--color-success`        | `#34d399`        |
| `--color-danger`         | `#f87171`        |
| `--font-mono`            | Space Mono       |
| `--font-sans`            | DM Sans          |
