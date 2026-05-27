# Ghost Coach Backend

AI-powered stance analysis & coaching for sports athletes.
Spring Boot 4 + Java 21 + PostgreSQL 16 + Gemini Vision API.

---

## Stack

| Layer | Choice |
|---|---|
| Runtime | Java 21 (LTS) |
| Framework | Spring Boot 4.0.6 (Web, Security, Data JPA, Cache, Actuator, Validation) |
| Build | Gradle (Kotlin DSL) |
| Database | PostgreSQL 16, Flyway migrations (V1–V7) |
| ORM | JPA / Hibernate + HikariCP (env-driven pool) |
| Auth | JWT HS256 (`iss` / `aud` / `jti` claims, fail-fast on short secret) |
| AI | Google Gemini (`gemini-2.5-flash-lite` default) via `RestClient` |
| Caching | Caffeine (`prompts`, `system-vars`) |
| Storage | Local filesystem (`UPLOAD_DIR`) with magic-byte MIME validation |
| Docs | springdoc-openapi (Swagger UI at `/swagger-ui.html`) |
| Tests | JUnit 5 + Mockito + Testcontainers (Postgres), JaCoCo 60% gate |

Current test suite: **158 tests** · **~91% line / ~84% branch** coverage.

---

## Prerequisites

- Java 21
- PostgreSQL 16 (locally or via Docker)
- Gemini API key from [aistudio.google.com](https://aistudio.google.com)

## Setup

```bash
# 1. Configure environment
cp .env.example .env
#   - JWT_SECRET must be >= 32 characters (HS256 requirement)
#   - GEMINI_API_KEY required

# 2. Create database
createdb ghostcoach

# 3. Run
./gradlew bootRun
```

App: `http://localhost:8080` · Swagger UI: `/swagger-ui.html` · Health: `/actuator/health`.

### Gradle Tasks

| Command | Purpose |
|---|---|
| `./gradlew bootRun` | Run app (dev profile) |
| `./gradlew test` | Unit + slice tests (fast, no Docker) |
| `./gradlew integrationTest` | Testcontainers-based tests (Docker required) |
| `./gradlew jacocoTestReport` | Coverage report → `build/reports/jacoco` |
| `./gradlew build` | Compile + test + package |
| `./gradlew bootJar` | Build `ghost-coach-be.jar` |

### Environment Variables

| Variable | Default | Required |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:6432/ghostcoach` | No |
| `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` | No |
| `DB_POOL_SIZE` / `DB_POOL_MIN_IDLE` | `10` / `2` | No |
| `JWT_SECRET` | — | **Yes** (min 32 chars) |
| `JWT_EXPIRATION_HOURS` | `2` | No |
| `JWT_ISSUER` / `JWT_AUDIENCE` | `ghost-coach` / `ghost-coach-client` | No |
| `GEMINI_API_KEY` | — | **Yes** |
| `UPLOAD_DIR` | `./uploads` | No |
| `APP_MAX_FILE_SIZE` | `5MB` | No |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | No |
| `PROMPT_CACHE_SPEC` | `maximumSize=50,expireAfterWrite=5m` | No |

### API Surface

Base: `/api/v1`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/auth/register` | — | Register player + profile (sport, position) |
| POST | `/auth/login` | — | Return JWT |
| GET  | `/users/me` | JWT | Current user profile |
| POST | `/sessions` | JWT | Upload stance image → AI feedback |
| GET  | `/sessions` | JWT | History (paginated, validated `page`/`size`) |
| GET  | `/sessions/{id}` | JWT | Session detail |
| GET  | `/sessions/{id}/image` | JWT | Serve uploaded image |
| POST | `/sessions/{id}/chat` | JWT | Send chat message (sliding-window memory) |
| GET  | `/sessions/{id}/chat` | JWT | Chat history (paginated) |
| GET  | `/system-vars/{category}` | JWT | Lookup data (e.g. positions per sport) |
| GET  | `/actuator/health` | — | Liveness |

---

## Running with Docker

All compose commands are run **from the project root** and load `.env` via
`--env-file .env -f docker/docker-compose.yml`.

### Production-like

```bash
docker compose --env-file .env -f docker/docker-compose.yml up --build
```

- App: `http://localhost:8080`
- Postgres: `localhost:6432` (mapped from container `5432`)
- Image: multi-stage Temurin 21 (JDK build → JRE runtime), non-root user, layered jar.

Stop: `docker compose -f docker/docker-compose.yml down` (`-v` to wipe DB volume).

### Debug + Hot Reload

`docker-compose.debug.yml` combines three things:

1. **JDWP** remote debug on port `5005`.
2. **Gradle continuous build** (`./gradlew -t classes`) inside the container.
3. **Spring DevTools** in-place context restart (~1–2 s after class change).

```bash
docker compose --env-file .env \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.debug.yml \
  up --build
```

Attach: IntelliJ → *Remote JVM Debug* / VS Code → *Attach to Docker (5005)*.
Set `DEBUG_SUSPEND=y` in the override to pause boot until the debugger attaches.

| Port | Purpose |
|---|---|
| `8080` | HTTP API |
| `5005` | JDWP (debug override only) |
| `6432` | PostgreSQL |

> The dev image (`Dockerfile.dev`) bind-mounts `./src` — **local only, not for prod**.

---

## Key Architectural Decisions

| Area | Decision | Rationale |
|---|---|---|
| **Prompts** | Stored in `prompts` table (JSONB vars + `model_config`), rendered via `PromptTemplateRenderer`, cached with Caffeine, warmed on startup. | Edit prompts without redeploy; A/B variants and model config per prompt key. See plan `12-prompt-storage.md`. |
| **AI call boundary** | Gemini call lives **outside** `@Transactional` to avoid holding DB connections while a remote call is in flight. Persistence happens in a second step. | Connection-pool starvation + orphan-file safety. See plan `03-transaction-resource.md`. |
| **File validation** | Magic-byte sniff (Apache Tika) + extension check + `APP_MAX_FILE_SIZE` (single source for both Spring multipart cap and in-service check). | Prevents content-type spoofing; one knob for the limit. |
| **Image quality gate** | Gemini returns a `qualityCheck` field; insufficient quality is rejected with `HTTP 422` before persist. | Avoids storing garbage data and wasting later analysis. See plan `14-image-quality-check.md`. |
| **Chat memory** | Sliding window of last N=20 messages injected into the prompt (no vector store yet). | Cheap, deterministic, good enough for the current UX. See plan `15-chat-conversation-memory.md`. |
| **JWT** | HS256 with `iss` / `aud` / `jti`, validated on every request; secret length enforced at startup (fail-fast). | Standard claims for revocation and multi-audience readiness. |
| **Session service** | Split into `SessionAnalyzer` (AI) + `SessionPersister` (DB) + `SessionUrls` (link building). | Single responsibility, easier to test, easier to swap LLM provider later. |
| **Pagination** | `page` / `size` validated with `@Min` / `@Max`, response wrapped in `PageResponse<T>`. | Consistent envelope across `sessions` and `chat`. |
| **HikariCP** | Pool sizes / timeouts driven by env vars. | Tunable per environment without code changes. |
| **System vars** | Generic `system_vars` table with `category` / `scope` (e.g. positions per sport) and a `@ValidSportPosition` cross-field validator. | Avoids hard-coded enums for fast-changing lookup data. |

Implementation plans for the items above live in [`docs/impl-plans/`](docs/impl-plans/) (each prefixed by execution-order number).

---

### Next Steps Planned

1. **Free-tier quota enforcement.** _Why first: both the web and mobile roadmaps have quota UX waiting on this — without it, every other plan downstream looks incomplete._
   Hard caps per user: max **5 stance uploads** and max **5 chat messages per stance**.
   - DB: `usage_counters` table (or columns on `users`) tracking `stance_count` and `chat_count` per session.
   - Service: increment + check at `POST /sessions` and `POST /sessions/{id}/chat`; reject with `HTTP 429` (`QUOTA_EXCEEDED`) carrying `limit` / `used` / `resetAt` in the response payload.
   - Open question: per-account lifetime vs. rolling 30-day window (lean rolling 30d — friendlier to retention).
   - Effort: S · Risk: low · Unblocks: web Planned #1, mobile Planned #1.

2. **Async session processing + push hooks.** _Why: `POST /sessions` currently blocks for the Gemini round-trip (seconds). Moving it async unlocks mobile push, frees the request thread, and is the foundation for any future video analysis._
   Switch `POST /sessions` to return `HTTP 202 Accepted` with `{ sessionId, status: "PROCESSING" }`. Run Gemini analysis via Spring `@Async` (or a small queue) and emit a `feedback_ready` event the client can subscribe to (FCM topic / SSE / webhook).
   - Touches: `SessionService`, new `SessionEventPublisher`, status column on `coaching_sessions`, `GET /sessions/{id}` returns the current status.
   - Effort: M · Risk: medium (transaction boundary care; orphan-cleanup on failure) · Unblocks: mobile Planned #4 (FCM push).

3. **Progress analytics endpoint.** _Why: the web roadmap's `/progress` dashboard explicitly depends on this. Strong retention driver — visible improvement over time is the product's hook._
   New `GET /users/me/progress` returning aggregated trends: overall score (daily / weekly buckets), per-body-part averages, session frequency, and a "biggest improvement" delta.
   - Touches: read-only repository, materialized view or cached query (Caffeine, 1 h TTL).
   - Effort: M · Risk: low · Unblocks: web Planned #5 (Progress dashboard route).

4. **Streaming chat responses (SSE).** _Why: chat is the only blocking UI on web + mobile. Tokens streaming in feel instant — biggest perceived-latency win we can ship._
   Switch `POST /sessions/{id}/chat` to a Server-Sent Events stream so Gemini tokens arrive incrementally. Keep the existing endpoint as a fallback for clients that can't hold an SSE connection.
   - Touches: `ChatService` returns `Flux<String>` (or `SseEmitter`), Gemini streaming endpoint wired in `GeminiClient`, persist the full message on stream completion.
   - Effort: M · Risk: medium (back-pressure + connection lifecycle) · Unblocks: web Planned #4 (Streaming chat), mobile Parked SSE item.

5. **Smart LLM routing with fallback.** _Why: single-vendor risk on Gemini today — one outage or pricing change and the product stops. Plan already drafted in [`docs/impl-plans/13-multi-llm-routing.md`](docs/impl-plans/13-multi-llm-routing.md)._
   Provider abstraction over Gemini + OpenRouter with a per-prompt fallback chain, model registry (`llm_models` table), capability filtering (TEXT / JSON / VISION), and parse-retry on malformed structured output.
   - Touches: `LlmProvider` interface, `LlmRouter`, registry seed migration, retry policy config.
   - Effort: L · Risk: medium (response-shape drift across providers) · Unblocks: future video-analysis vision routing; reduces Gemini bill risk.

> Implementation plans will land under `docs/impl-plans/` continuing from `16-`.

### Parked (Not Now, But Considered)

Kept here so we don't re-debate them every sprint. Re-evaluate after the 5 Planned items ship.

- **Subscription tier (Stripe / Midtrans)** — wires the free-tier quota above to `FREE` / `PRO` / `COACH` plans. Sequenced after #1.
- **Video stance analysis (multi-frame)** — accept ≤ 5 s clips, extract N frames, run Gemini Vision across the sequence. Best after #2 (async) and #5 (vision routing) land.
- **Observability upgrade** — OpenTelemetry exporter, Micrometer Prometheus, structured JSON logs. Needed before scaling beyond a single instance.
- **Semantic session search (pgvector)** — embed feedback text, `GET /sessions/search?q=…`. Useful once a user has >50 sessions.
- **Reference-stance comparison** — pro-example seed data + delta feedback ("elbow is 15° too low vs. reference").
- **Streaks + gamification** — daily streak counter, milestone badges. Pairs with mobile push.
- **Per-user / per-IP rate limiting (Bucket4j)** — protects the Gemini bill independent of business-level quota.
- **Refresh-token rotation** — replace single-token / 2 h expiry with rotation + reuse detection.
- **Coach / trainer share tokens** — read-only share links per session. Opens a B2B angle; mobile + web roadmaps both reference this.