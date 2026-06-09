# PRMS — Agent guidance

## Before you start

Read `.cursor/rules/cursor-rules.mdc` (the canonical detailed guide for this repo). This file only adds what that one misses.

## Architecture: the 30-second map

- **Backend** (`src/`): Java 17, Spring Boot 3.5.x, JPA, PostgreSQL, JWT, OpenAPI. Main class `PrmsApplication.java`. Port **9999** (from `application.yml`, not the default 8080).
- **Frontend** (`prms-web/`): Angular 20 standalone, NG-ZORRO, `@ngx-translate`, RxJS 7.8. Port **4200**.
- Both live in one Maven project; `pom.xml` orchestrates the frontend build via `exec-maven-plugin` during `generate-resources`.

## Commands

| Where | What | Command |
|---|---|---|
| root | Run backend | `./mvnw spring-boot:run` |
| root | Run backend tests | `./mvnw test` |
| root | Full Maven build (includes frontend) | `./mvnw clean package -DskipTests` or `./mvnw clean package` |
| `prms-web/` | Install frontend deps | `npm install` |
| `prms-web/` | Start frontend dev server | `npm start` (runs `ng serve`) |
| `prms-web/` | Frontend tests | `npm test` (Karma) |
| root | Build with Docker | `docker build -f Dockerfile .` (multi-stage: Node 22 → Maven → JRE 17) |

## Setup gotchas

- **Windows**: Maven profile `windows` activates automatically and uses `npm.cmd` instead of `npm`.
- **Database**: PostgreSQL required. Default: `jdbc:postgresql://localhost:5432/prms`, user `postgres`, password `123456`. Override via env `PRMS_DATASOURCE_*`. JPA `ddl-auto: none` — schema must exist already.
- **Default admin**: `supper_admin` / `123456` (seeded by `DataInitializer` at startup).
- **FE API URL**: Not in `environment.ts`. Go to `prms-web/public/assets/config/app-config.json` (default `http://10.10.10.102:9999`).

## Non-obvious conventions

- **Backend port is 9999**, not 8080. (`server.port: ${PRMS_SERVER_PORT:9999}` in `application.yml`). The `.cursor/rules/cursor-rules.mdc` incorrectly says 8080.
- **Paged/search APIs use POST** (`POST /api/v1/<entity>/page`), not GET. Request body extends `SearchRequest`.
- **No `@Valid` in controllers**. Use `BaseService.validation(request)` instead. Field errors go in `body` as a map.
- **Soft delete** via `voided` boolean field on `BaseEntity`.
- **UUID PKs** use `Generators.timeBasedEpochGenerator()` (time-ordered), not random UUIDs.
- **Backend i18n**: Keys in `util/SystemMessage.java`, variable placeholders in `util/SystemVariable.java`. Resolve via `getMessage(key, args...)`. Messages: `i18n/messages.properties`, `i18n/messages_vi.properties`.
- **DTO constructors** prefer `BeanUtils.copyProperties(entity, this)`.
- **Native queries** must use Hibernate `addScalar()` + transformer/mapper, never `List<Object[]>`.
- **Frontend feature services** must return `this.base.get/post/...` directly (no extra `.pipe(map(...))` — `BaseService` already handles that). Components subscribe and read `res: ApiResponse`, then `res.body`.
- **Form validation from BE**: Use `[SERVER_FORM_ERROR_KEY]` computed property, not literal `{ SERVER_FORM_ERROR_KEY: ... }`. See `login.ts`.
- **Form submit** uses Reactive Forms (`[formGroup]` + `formControlName`). Filters can use `[(ngModel)]`.
- **Angular schematics skip tests** by default for components, services, guards, etc. (`skipTests: true`).
- **Frontend dev service worker** enabled even on localhost. `ngsw-worker.js` only generated in production builds.
- **Angular `strict: false`** in tsconfig.json — but `strictTemplates: true` in angularCompilerOptions.

## Existing instruction files

- **`.cursor/rules/cursor-rules.mdc`** — The comprehensive reference. 468 lines, covers BE/FE conventions, auth flow, domain guides, patterns. If you read one instruction file, read this.
- **`.aiassistant/rules/codebase-rules.md`** — Older, slightly different version of the same rules. When in doubt, prefer `.cursor/rules/cursor-rules.mdc`.

## What to verify before committing

1. BE ↔ FE DTO/model field names still agree.
2. No `@Valid` introduced in controllers.
3. Feature service returns `this.base.*` directly (no extra pipe).
4. `SystemMessage`/`SystemVariable` keys exist in `.properties` files for new messages.
5. Backend i18n messages added to both `messages.properties` (English fallback) and `messages_vi.properties`.
6. Frontend i18n added to both `en.json` and `vi.json` for new labels.
