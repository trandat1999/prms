---
description: PRMS fullstack guardrails for all tasks (BE + FE)
alwaysApply: true
---

# PRMS Codebase Rule (Mandatory)

Before doing any task in this repo, read and follow this rule.
If user request conflicts with this file, ask to confirm scope first.

## 1) Project map

- Backend (Spring Boot): `src/main/java/com/tranhuudat/prms` + `src/main/resources`
- Frontend (Angular): `prms-web/src/app` + `prms-web/public/assets`
- Backend runs on `http://localhost:8080`
- Frontend runs on `http://localhost:4200`
- FE API base URL comes from `prms-web/public/assets/config/app-config.json`

## 2) Backend stack and architecture

- Java 17, Spring Boot 3.5.x, PostgreSQL, Spring Security, JWT, OpenAPI.
- Layering is mandatory: `controller -> service -> repository`.
- Do not call repository directly from controller.
- Service impl classes should extend `BaseService` where possible.
- API response contract uses `BaseResponse` with fields:
  - `timestamp`, `body`, `message`, `status`, `code`
- For paged search, keep pattern:
  - endpoint: `POST /api/v1/<entity>/page`
  - request extends `SearchRequest`
- Validation pattern currently used:
  - call `validation(request)` from `BaseService`
  - return 400 response body with field-message map when invalid
- Keep package namespace under `com.tranhuudat.prms`.

## 3) Security and auth constraints

- Public endpoints are controlled by `SecurityConfig.PUBLIC_ENDPOINTS`.
- JWT auth filter runs before `UsernamePasswordAuthenticationFilter`.
- CORS currently allows `http://localhost:4200`.
- Do not widen auth/cors rules unless task explicitly asks for it.

## 4) Frontend stack and architecture

- Angular 20 standalone components (no feature NgModule creation).
- UI: NG-ZORRO, i18n with `@ngx-translate/core`, spinner with `ngx-spinner`.
- Keep feature structure inside `pages/<feature>/...`.
- Keep feature models in `pages/<feature>/models/*`.
- Keep feature services in `pages/<feature>/services/*` (or core/shared if truly global).
- API calls must stay in services. Components do not call `HttpClient` directly.
- Use `BaseService` in `core/services/base-service.ts` for HTTP calls.

## 5) Shared input rule (important)

- Reuse `app-input` (`shared/input/input.ts`) for form/filter input UI.
- Do not add raw `nz-input`, `nz-select`, `nz-date-picker`, etc. in pages if `app-input` can cover.
- If missing behavior, extend `InputCommon` first, then consume from pages.

## 6) Data and integration contract (BE <-> FE)

- Project API base path is `/api/v1/projects`.
- FE expects BE wrapped response shape (`ApiResponse`) compatible with `BaseResponse`.
- Keep enum values and field names aligned between:
  - BE DTO/enums in `src/main/java/...`
  - FE models/types in `prms-web/src/app/pages/.../models`
- Do not rename payload fields without updating both sides in same task.

## 7) Coding style and hygiene

- Prefer constructor injection.
- No `System.out.println` in backend.
- Keep logging via SLF4J.
- Keep FE formatting consistent with Prettier in `prms-web/package.json`:
  - `printWidth: 100`, `singleQuote: true`.
- Do not create duplicate Windows-style source paths like `src\main\java\...`.
- Do not commit runtime/build artifacts (`target`, `dist`, logs) in code changes.

## 8) Task execution checklist

For each implementation task:

1. Identify impacted layers/files (BE, FE, or both).
2. Preserve existing API contract unless task asks to change it.
3. Update DTO/model/type/enum consistently across BE and FE.
4. Add or adjust validation/error handling with current patterns.
5. Verify build/test at least for touched side:
   - BE: `./mvnw test` (or targeted tests)
   - FE: `npm run build` (inside `prms-web`)
6. Return changed file list and highlight any contract changes.
