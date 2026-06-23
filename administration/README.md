# Sprint Reporter — Admin Panel

Standalone Angular 18 admin panel for managing organization subscriptions. Accessible only to super-admins (ADMIN JWT role).

## Prerequisites

- Node.js 18+
- npm 9+
- Angular CLI 18: `npm install -g @angular/cli@18`

## Setup

```bash
cd administration
npm install
```

## Development server

```bash
ng serve
# or
npm start
```

Navigate to `http://localhost:4201/`. The app will auto-reload on file changes.

## Production build

```bash
ng build
```

Build artifacts will be stored in `dist/sprint-reporter-admin/`.

---

## Configuration

| File | Purpose |
|------|---------|
| `src/environments/environment.ts` | Dev — `apiUrl: 'http://localhost:8080/api/v1'` |
| `src/environments/environment.prod.ts` | Prod — `apiUrl: '/api/v1'` |

---

## Authentication

1. Navigate to `http://localhost:4201/login`
2. Enter your super-admin credentials
3. The JWT must carry the `ADMIN` role — regular user credentials will be rejected by the backend

The token is stored in `localStorage` under the key `admin_jwt_token`. It is automatically attached to every API request via the JWT interceptor. On 401 responses the app auto-logs-out.

---

## Features

| Feature | Details |
|---------|---------|
| **Login page** | Email + password, calls `POST /api/v1/auth/login` |
| **Organization table** | Lists all orgs with subscription status, plan, type, pilot expiry |
| **Assign Enterprise** | Opens dialog → `POST /api/v1/subscription/admin/enterprise?organizationId=…` |
| **Assign Pilot** | Opens dialog with date picker → `POST /api/v1/subscription/admin/pilot?organizationId=…` |
| **Convert to Self-service** | Confirm dialog → `POST /api/v1/subscription/admin/convert-pilot?organizationId=…` |
| **Activate / Deactivate** | `POST /api/v1/subscription/admin/activation?active=true|false&organizationId=…` |

> **Note:** The `?organizationId=uuid` query param is forwarded on all admin endpoints. The backend needs to be updated to read and honor this param for cross-org management.

---

## Project structure

```
administration/
├── angular.json
├── package.json
├── tailwind.config.js
├── tsconfig.json / tsconfig.app.json
└── src/
    ├── environments/
    │   ├── environment.ts
    │   └── environment.prod.ts
    ├── index.html
    ├── main.ts
    ├── styles.scss
    └── app/
        ├── app.component.ts      # Shell with <router-outlet>
        ├── app.config.ts         # Providers (HttpClient, Router, Animations, DateAdapter)
        ├── app.routes.ts         # /login → LoginComponent, /dashboard → DashboardComponent
        ├── guards/
        │   └── auth.guard.ts
        ├── interceptors/
        │   ├── jwt.interceptor.ts
        │   └── error.interceptor.ts
        ├── services/
        │   ├── auth.service.ts
        │   ├── organization.service.ts
        │   └── subscription-admin.service.ts
        ├── login/
        │   ├── login.component.ts
        │   └── login.component.html
        └── dashboard/
            ├── dashboard.component.ts
            ├── dashboard.component.html
            ├── assign-enterprise-dialog.component.ts / .html
            ├── assign-pilot-dialog.component.ts / .html
            └── confirm-dialog.component.ts / .html
```
