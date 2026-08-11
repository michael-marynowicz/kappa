# Deployment Guide

This repository is a monorepo with a separate Angular frontend and Spring Boot backend. The most stable production setup is:

- Frontend: Vercel
- Backend: Render or Railway
- Database: managed PostgreSQL (Neon or Supabase)

## Recommended production flow

1. Deploy the backend first.
2. Create the PostgreSQL database and wire it to the backend.
3. Deploy the frontend once the backend URL is known.
4. Set CORS and external callbacks to the final production URLs.

## Backend deployment

Use the root-level [Dockerfile](Dockerfile) when the platform builds from the repository root.

If the platform supports a root directory setting and you prefer the backend-only Dockerfile, use:

- Root directory: `backend`
- Dockerfile path: `Dockerfile`

If the platform builds from the monorepo root, use:

- Root directory: repository root
- Dockerfile path: `Dockerfile`

### Backend environment variables

Set the production variables from [backend/src/main/resources/application-prod.yml](backend/src/main/resources/application-prod.yml).

Minimum required values:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`
- `APP_BASE_URL`
- `JIRA_BASE_URL`
- `JIRA_USER_EMAIL`
- `JIRA_PAT`

If you use email or billing features, also set:

- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM`
- `STRIPE_ENABLED`
- `STRIPE_SECRET_KEY`
- `STRIPE_WEBHOOK_SECRET`
- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`
- `ATLASSIAN_CLIENT_ID`
- `ATLASSIAN_CLIENT_SECRET`

## Frontend deployment

Use Vercel with the frontend folder as the project root.

Suggested settings:

- Root directory: `frontend`
- Build command: `npm run build:prod`
- Output directory: `dist/sprint-reporter-ui/browser`

The frontend currently uses `environment.prod.ts` for the API base URL. Before the final production deploy, point it to the real backend URL or switch to a runtime configuration strategy.

## First things to do

1. Create the PostgreSQL database.
2. Deploy the backend.
3. Copy the backend public URL.
4. Update the frontend API base URL.
5. Deploy the frontend.
6. Verify CORS, login, Jira access, and CSV export.

## Why this approach

This avoids ambiguous build contexts and keeps the backend and frontend deployment lifecycles separate. It is simpler to debug and easier to maintain than forcing both apps through a single platform configuration.
