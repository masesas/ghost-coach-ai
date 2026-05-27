# Ghost Coach

AI-powered stance analysis & coaching for athletes. Monorepo with three projects sharing one backend API.

| Project | Stack | Default URL |
|---|---|---|
| [`ghost-coach-be`](ghost-coach-be) | Spring Boot 4 · Java 21 · PostgreSQL 16 · Gemini Vision | `http://localhost:8080` |
| [`ghost-coach-web`](ghost-coach-web) | React 18 · Vite 5 · TypeScript · Tailwind · TanStack Query | `http://localhost:5173` |
| [`ghost-coach-mobile`](ghost-coach-mobile) | Flutter 3.38 · Bloc · go_router · Dio | Android/iOS |

---

## Quick Start (Docker, full stack)

```bash
cp ghost-coach-be/.env.example ghost-coach-be/.env
# edit ghost-coach-be/.env → set JWT_SECRET (>= 32 chars) + GEMINI_API_KEY

make dev      # BE (hot reload + JDWP :5005) + Postgres + FE (Vite HMR)
make stop     # stop everything
make clean    # stop + drop volumes (DB data, uploads)
make logs     # tail all containers
make status   # docker ps for gc-* containers
```

After `make dev`:
- Backend: <http://localhost:8080> · Swagger: `/swagger-ui.html` · Debug: `:5005`
- Frontend: <http://localhost:5173>
- Postgres: `localhost:6432` (user/pw: `postgres` / `postgres`, db: `ghostcoach`)

Other targets: `make dev-be`, `make dev-fe`, `make prod`, `make rebuild`, `make db`, `make shell-be`, `make shell-fe`. Run `make help` for the full list.

---

## Run Locally (without Docker)

### Backend
```bash
cd ghost-coach-be
cp .env.example .env                          # set JWT_SECRET + GEMINI_API_KEY
createdb ghostcoach                           # PostgreSQL 16 required
./gradlew bootRun                             # → :8080
./gradlew test integrationTest                # unit + Testcontainers
```

### Web
```bash
cd ghost-coach-web
npm install
npm run dev                                   # → :5173 (proxies /api → :8080)
npm run build && npm run preview              # prod build
```

### Mobile
```bash
cd ghost-coach-mobile
flutter pub get
dart run build_runner build --delete-conflicting-outputs   # go_router codegen
make run-dev                                  # uses 10.0.2.2:8080 (Android emulator)
# iOS Simulator / physical device:
flutter run -t lib/main_dev.dart --dart-define=API_BASE_URL=http://<LAN-IP>:8080/api/v1
make apk-prod                                 # release APK
```

---

## What's in each project

### `ghost-coach-be` — Spring Boot API
JWT auth (HS256, `iss`/`aud`/`jti`), Gemini Vision analysis, Caffeine-cached prompt templates loaded from DB, sliding-window chat memory (N=20), magic-byte file validation, Flyway migrations V1–V7, JaCoCo 60% gate (currently ~91% line / ~84% branch, 158 tests).

Routes under `/api/v1`: `/auth/{register,login}`, `/users/me`, `/sessions` (upload + history + image + chat), `/system-vars/{category}`, `/actuator/health`.

Gemini call lives outside `@Transactional` to avoid holding DB connections during the remote round-trip. Image quality gate rejects unusable photos with `HTTP 422` before persist.

### `ghost-coach-web` — React SPA
Athlete flow: register (sport + position from `/system-vars`) → upload stance image → see feedback report → chat about it → review progress chart on `/history`.

JWT in `localStorage` (attached by Axios interceptor), authenticated images via `AuthImage` blob trick, all I/O through TanStack Query hooks, React Hook Form + Zod, Tailwind + `cva` primitives in `components/ui/`.

### `ghost-coach-mobile` — Flutter app
Mirrors the web feature set: auth, upload (camera/gallery, 5 MB + MIME check matching the BE), paginated history with `fl_chart` progress chart, session detail + chat panel, bottom-nav shell.

Bloc per feature, `go_router` typed routes via `@TypedGoRoute` codegen, single Dio with auth/error/chucker interceptors, three flavors (`main_dev` / `main_staging` / `main_prod`) feeding an `AppEnv` enum, `--dart-define=API_BASE_URL=…` for LAN/device overrides.

---

## Required environment

| Variable | Where | Notes |
|---|---|---|
| `JWT_SECRET` | `ghost-coach-be/.env` | Must be ≥ 32 chars (HS256 fail-fast) |
| `GEMINI_API_KEY` | `ghost-coach-be/.env` | From <https://aistudio.google.com> |
| `VITE_API_BASE_URL` | `ghost-coach-web/.env` | Defaults to `http://localhost:8080/api/v1` |
| `API_BASE_URL` (dart-define) | mobile run flag | Override per device; emulator default is `10.0.2.2:8080` |

Full BE env reference: [`ghost-coach-be/README.md`](ghost-coach-be/README.md#environment-variables).

---

## Repo layout

```
.
├── Makefile                  # full-stack docker orchestration (dev / prod / debug / logs / db)
├── ghost-coach-be/           # Spring Boot API + Flyway + Docker compose
│   └── docker/               # docker-compose.yml + docker-compose.debug.yml
├── ghost-coach-web/          # React SPA + Vite + nginx prod image
│   └── docker/               # docker-compose.yml + docker-compose.dev.yml
└── ghost-coach-mobile/       # Flutter app (Android + iOS, 3 flavors)
```

Each project has its own `README.md` with stack details, environment, architecture decisions, and roadmap.
