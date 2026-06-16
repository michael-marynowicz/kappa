# Security And Quality Policy

Version: 1.0
Scope: Full repository (backend, frontend, Docker, CI)
Last updated: 2026-06-16

## 1) Purpose

This document defines mandatory security and quality requirements for this project.
It is the reference used for local development, pull request reviews, and CI gates.

Goals:
- Protect credentials, source code, and runtime data.
- Keep regressions low through repeatable quality checks.
- Make release decisions evidence-based, not assumption-based.

## 2) Current Critical Risk (Immediate Action)

Status observed in repository:
- A Jira credential appears to be present in backend/src/main/resources/application.yml.

Mandatory immediate actions:
1. Revoke and rotate the exposed credential now.
2. Remove all hardcoded credentials from tracked files.
3. Move secrets to environment variables or secret manager.
4. Rewrite git history if sensitive values were pushed to remote.
5. Verify revocation and rotation in Jira audit logs.

No release is allowed until this is completed.

## 3) Security Baseline

### 3.1 Secrets Management (Mandatory)

Rules:
- Never commit tokens, passwords, PATs, API keys, or private endpoints requiring auth context.
- Do not store secrets in application.yml, .env committed files, Dockerfile, compose files, or test fixtures.
- Use environment variables for local/dev and a secret manager for shared/staging/prod.
- Use least privilege and short-lived credentials when possible.

Examples of expected variables:
- JIRA_BASE_URL
- JIRA_USER_EMAIL
- JIRA_API_TOKEN
- JIRA_SPRINT_ID
- JIRA_MOCK_MODE

Protection controls:
- Pre-commit secret scanning.
- CI secret scanning on every PR and push.
- Branch protection that blocks merge on secret detection.

### 3.2 Dependency Security

Backend (Maven):
- Track dependency vulnerabilities on every PR.
- Block merge on Critical and High vulnerabilities unless approved exception exists.
- Keep Spring Boot and transitive dependencies patched.

Frontend (npm/Angular):
- Run npm audit in CI with fail thresholds.
- Keep Angular, RxJS, and tooling updated.
- Remove unused dependencies to reduce attack surface.

### 3.3 Input Validation And Output Safety

Backend:
- Validate all request DTOs using bean validation.
- Reject invalid or out-of-range story points.
- Sanitize and encode content before export if needed.
- Return controlled error payloads without stack traces.

Frontend:
- Avoid bypassing Angular template protections.
- Avoid direct DOM injection and unsafe HTML rendering.
- Validate user-entered values before API calls.

### 3.4 HTTP, CORS, And Transport

- Restrict CORS by environment, never wildcard in production.
- Enforce HTTPS in non-local environments.
- Set secure response headers at API and nginx levels.
- Disable verbose debug logs in production.

### 3.5 Logging And Privacy

- Never log tokens, credentials, or raw auth headers.
- Log only required business and technical context.
- Use structured logs where possible.
- Keep retention aligned with organizational policy.

## 4) Quality Baseline

### 4.1 Architecture And Code Quality

- Keep clear separation between controller, service, domain, and infrastructure layers.
- Keep business rules in service/domain, not in controllers.
- Keep frontend HTTP access in services, not in presentational components.
- Prefer small units, clear naming, and single responsibility.

### 4.2 Testing Requirements

Backend:
- Unit tests for domain and service logic.
- Controller tests for API contracts and error paths.
- CSV export tests for format and escaping correctness.

Frontend:
- Unit tests for state service, API service contract assumptions, and critical components.
- Edge case tests for invalid values and API error handling.

Minimum merge gate (default):
- All tests pass.
- No flaky failures accepted.
- New or changed business logic must include tests.

### 4.3 Static Analysis And Formatting

- Backend: build and tests must pass with zero compilation warnings that indicate risk.
- Frontend: lint must pass and production build must pass.
- PRs with unchecked warnings are not considered done.

### 4.4 Performance And Reliability Checks

- Basic startup check for backend and frontend before merge.
- API endpoint smoke checks for issues list, update, and csv export.
- Validate Docker compose startup path at least once for release branches.

## 5) Mandatory Verification Commands

Run from repository root unless specified.

Backend:
- cd backend
- ./mvnw clean test
- ./mvnw -q package -DskipTests

Frontend:
- cd frontend
- npm ci
- npm run lint
- npm test -- --watch=false
- npm run build:prod

Docker path:
- docker compose build
- docker compose up -d
- docker compose ps

Security checks (recommended minimum):
- Frontend: npm audit --audit-level=high
- Backend: dependency vulnerability scan integrated in CI
- Secret scanning: gitleaks or equivalent in pre-commit and CI

## 6) Pull Request Gate (Definition Of Done)

A PR is mergeable only if all items are true:
1. No secrets introduced and secret scan passes.
2. Backend tests pass.
3. Frontend lint, tests, and prod build pass.
4. No unresolved High/Critical vulnerabilities, or approved exception exists.
5. Changes include tests for business logic impact.
6. Reviewer checklist completed.

Reviewer checklist:
- Security impact reviewed.
- Input validation and error handling verified.
- Logging does not leak sensitive data.
- Backward compatibility considered for API payloads.
- Rollback path is clear.

## 7) CI/CD Policy (Required)

Pipeline stages:
1. Checkout and dependency install
2. Secret scanning
3. SAST and dependency scanning
4. Backend tests
5. Frontend lint and tests
6. Frontend prod build
7. Docker build and smoke checks

Branch protection recommendations:
- Require PR review before merge.
- Require all status checks to pass.
- Disallow force-push on protected branches.
- Require resolved conversations.

## 8) Exception Process

If a rule cannot be met:
1. Open a time-bound exception record in PR description.
2. Provide risk, compensating controls, and expiration date.
3. Get explicit approval from repository owner.
4. Track remediation task before release.

No permanent exception for committed secrets.

## 9) Ownership

Policy owner: Repository maintainers.
Review cadence: at least once per quarter, and after every security incident.

## 10) Quick Start For Contributors

Before opening a PR:
1. Pull latest changes.
2. Run backend tests.
3. Run frontend lint, tests, and prod build.
4. Verify no secret is introduced.
5. Confirm changed logic includes tests.

If any check fails, fix first, then request review.
