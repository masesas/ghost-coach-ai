# Ghost Coach Web

React SPA for athletes to upload a stance photo, receive AI coaching feedback, and chat about it.
React 18 + Vite 5 + TypeScript + Tailwind + TanStack Query.

---

## Stack

| Layer | Choice |
|---|---|
| Runtime | Node.js 18+ (tested on 24) |
| Framework | React 18.3 + React Router 6 |
| Build | Vite 5 (path alias `@/* → src/*`) |
| Language | TypeScript 5.6 (strict) |
| Styling | Tailwind 3.4 + `clsx` / `tailwind-merge` / `class-variance-authority` |
| Data | TanStack Query 5 (cache + retry) + Axios (JWT interceptor) |
| Forms | React Hook Form + Zod resolvers |
| Charts | Recharts 2 |
| UX | `sonner` (toasts), `lucide-react` (icons) |
| Lint / Format | ESLint 9 (flat config) + Prettier 3 + `prettier-plugin-tailwindcss` |

---

## Prerequisites

- Node.js 18+
- Backend running at `http://localhost:8080` (see `../ghost-coach-be`)

## Setup

```bash
# 1. Install
npm install

# 2. (Optional) env — defaults already point at localhost:8080
cp .env.example .env

# 3. Run
npm run dev
```

App: `http://localhost:5173`.

### Scripts

| Command | Purpose |
|---|---|
| `npm run dev` | Vite dev server (HMR + `/api` proxy → backend) |
| `npm run build` | Type-check (`tsc -b`) + production build |
| `npm run preview` | Preview production build locally |
| `npm run lint` | ESLint |
| `npm run format` | Prettier write `src/**/*.{ts,tsx,css}` |

### Environment

| Variable | Default | Purpose |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1` | Axios base URL |
| `VITE_API_PROXY_TARGET` | `http://localhost:8080` | Vite dev proxy target |
| `VITE_HMR_HOST` / `VITE_HMR_PORT` / `VITE_HMR_CLIENT_PORT` | `localhost` / `5173` / `5173` | HMR over Docker / tunnels |
| `CHOKIDAR_USEPOLLING` | unset | Set `true` if HMR doesn't fire on macOS / Windows / Docker |

### Routing

| Path | Guard | Page |
|---|---|---|
| `/login`, `/register` | public-only (redirect if logged in) | `LoginPage`, `RegisterPage` |
| `/` | auth | redirect → `/upload` |
| `/upload` | auth | `UploadPage` — pick image, analyze, see feedback |
| `/history` | auth | `HistoryPage` — `ProgressChart` + paginated `SessionCard` grid |
| `/history/:id` | auth | `SessionDetailPage` — feedback report + chat panel |

### Project Layout

```
src/
├── components/
│   ├── ui/           # button, card, input, badge, skeleton, … (cva-driven primitives)
│   ├── layout/       # AppShell, ProtectedRoute, PublicRoute
│   ├── feedback/     # FeedbackReportView, PriorityFixCard, ScoreBadge, ConfidenceBadge
│   └── common/       # AuthImage, EmptyState, ErrorState, LoadingSpinner
├── features/
│   ├── auth/         # login / register pages + hooks
│   ├── upload/       # ImageDropzone + AnalyzingState + UploadPage
│   ├── history/      # SessionCard, HistoryPage, SessionDetailPage, useSessions(ForChart)
│   ├── chat/         # ChatPanel, ChatInput, MessageBubble, useChat
│   ├── progress/     # ProgressChart (Recharts)
│   └── systemVars/   # useSystemVars (positions per sport)
├── lib/              # api (Axios+interceptors), queryClient, constants, schemas (Zod), utils (cn)
├── providers/        # AuthProvider, QueryProvider
├── types/            # api, user, session, chat, systemVar
└── styles/           # globals.css (Tailwind)
```

---

## Running with Docker

The backend must be up first so the web container can reach it on a shared network:

```bash
cd ../ghost-coach-be
docker compose --env-file .env -f docker/docker-compose.yml up -d
```

### Dev mode (HMR)

```bash
docker compose -f docker-compose.dev.yml up --build
```

- `src/**` bind-mounted; Vite HMR reloads without losing component state.
- If HMR doesn't fire on macOS / Windows, add `CHOKIDAR_USEPOLLING: "true"` to the compose env.

### Production-like (nginx)

```bash
docker compose up --build
```

- Multi-stage build (Node → nginx), SPA fallback to `index.html`, static assets cached 7 days.
- `/api/*` reverse-proxied to `gc-backend:8080` via the shared `ghost-coach-be_default` network.

| Port | Mode | Purpose |
|---|---|---|
| `5173` | dev | Vite + HMR |
| `5173` | prod | nginx (mapped from container `80`) |

Stop: `docker compose down` (`-v` to also drop the `node_modules` volume).

---

## Key Frontend Decisions

| Area | Decision | Rationale |
|---|---|---|
| **Auth storage** | JWT in `localStorage` under `ghostcoach.token`, attached by Axios request interceptor. | Survives full refresh; trade-off accepted — XSS risk mitigated by no third-party scripts and React-escaped output. Can swap for `HttpOnly` cookie when the BE issues one. |
| **Authenticated images** | Custom `AuthImage` fetches the URL with the bearer header, turns the response into a blob, and sets `<img src={blobUrl}>`. | Native `<img>` cannot send `Authorization`; this avoids exposing images via signed URLs or session cookies. |
| **Data layer** | All network access goes through TanStack Query hooks (`features/*/hooks/`). No `useEffect(fetch)` in components. | Cache, retry, dedupe, and loading/error states come for free; components stay declarative. |
| **API envelope** | Generic `unwrap<T>()` helper unboxes the `{ success, data, error }` envelope from the backend before reaching components. | Components never deal with the envelope shape — same `data` shape regardless of endpoint. |
| **Routing guards** | `ProtectedRoute` / `PublicRoute` are layout routes wrapping `AppShell`. Logged-in users on `/login` redirect to `/upload`; anonymous users on protected paths redirect to `/login`. | Guards live in the router tree, not inside each page. |
| **Forms** | React Hook Form + Zod schemas in `lib/schemas.ts`. | Single source of truth for validation; client errors mirror what the BE would reject. |
| **Styling** | Tailwind utility-first + `cn()` helper (`clsx` + `tailwind-merge`) + `cva` for variant components in `components/ui/`. | No CSS modules to maintain; variants stay type-safe. |
| **Path alias** | `@/*` → `src/*` in both `tsconfig` and `vite.config`. | Imports stay stable when files move. |
| **Progress chart** | `ProgressChart` lives in `features/progress/`, but the **data hook** that feeds it (`useSessionsForChart`, page 0 / size 50) lives in `features/history/`. | The chart is currently only rendered on `/history`; the hook is co-located with its consumer, the chart stays a reusable presentational component. |
| **Pagination** | `useSessions` reads the BE `PageResponse<SessionSummary>` envelope and exposes `data.content`, `data.page`, `data.totalPages`. Page size for history is small (default 12), chart pulls a larger window. | Two queries, two cache keys — list pagination doesn't refetch the chart. |
| **Toasts** | `sonner` for non-blocking feedback (success / error). | Lighter than building a notification system; consistent with shadcn-style stack. |

---

### Next Steps Planned

1. **Quota-aware UX (matches the BE free-tier enforcement).** _Why first: the BE plan ships the cap; the app must not look broken when it hits._
   When the backend returns `HTTP 429 QUOTA_EXCEEDED`, show a friendly banner with the remaining count and a CTA (e.g. "5/5 stances used — upgrade to keep coaching"). Reuse the response payload (`limit`, `used`, `resetAt`) — never compute client-side.
   - Touches: new `useUserQuota()` hook (TanStack Query, cached per route navigation), `QuotaBanner` in `components/feedback/`, surfaced in `UploadPage` (disable submit) and `ChatPanel` (disable input + tooltip).
   - Effort: S · Risk: low · Depends on: BE plan `16-free-tier-quota.md`.

2. **PWA + installable app.** _Why: athletes shoot stance photos on a phone — an installable PWA with offline shell is the cheapest path to a "native-feeling" experience without shipping a separate mobile build._
   Add `vite-plugin-pwa` with a service worker that precaches the shell + static assets, a web manifest with icons / theme color / display `standalone`, and an "Install" CTA when `beforeinstallprompt` fires.
   - Touches: `vite.config.ts` plugin block, `public/manifest.webmanifest`, install-prompt hook in `AppShell`.
   - Effort: S · Risk: low · Depends on: nothing. Sets up cache strategy for #4.

3. **Mobile camera capture flow.** _Why: single biggest conversion lift for first-time users — most drop-offs happen at "pick an image"._
   Today `ImageDropzone` only accepts files. Add a mobile-first capture screen with `<input capture="environment">` plus a live `MediaStream` preview (capture / retake / crop), keeping the desktop drag-and-drop intact.
   - Touches: new `CameraCapture` component, branch in `UploadPage` based on `navigator.mediaDevices` availability, fallback to file input.
   - Effort: M · Risk: medium (iOS Safari `getUserMedia` quirks) · Depends on: nothing. Best paired with #2 so it installs to the home screen.

4. **Streaming chat (SSE).** _Why: the only blocking UI in the app — chat currently waits seconds for the full response. Tokens streaming in feel instant._
   Switch `POST /sessions/{id}/chat` to an SSE stream and add a `useChatStream` hook on top of the existing `useChat`. Reuses the same Axios base config; falls back to the current request/response path if the browser drops the stream.
   - Touches: new `useChatStream` hook (native `EventSource`), `MessageBubble` token-append state, error/abort handling.
   - Effort: M · Risk: medium (Axios doesn't do SSE — native `EventSource` + auth header proxy required) · Depends on: BE streaming endpoint.

5. **Progress dashboard route.** _Why: the existing `ProgressChart` is already shipped but visually buried above the history grid; promoting it surfaces the product's main retention hook (visible improvement over time)._
   New `/progress` route with: overall score trend (line), per-body-part radar (`RadarChart`), session count per week (bar), and a "biggest improvement" highlight card. Hooks into the BE's planned `/users/me/progress` aggregate.
   - Touches: new `features/progress/pages/ProgressPage.tsx`, new `useProgress()` hook, nav entry in `AppShell`.
   - Effort: M · Risk: low · Depends on: BE `/users/me/progress` endpoint.

> Implementation plans will land under `docs/impl-plans/web/` following the BE numbering convention.

### Parked (Not Now, But Considered)

Kept here so we don't re-debate them every sprint. Re-evaluate after the 5 Planned items ship.

- Offline-first session list (IndexedDB via TanStack Query persister) — natural follow-up to #2
- Optimistic chat updates (`onMutate` + rollback) — superseded by #4 if SSE lands first
- Skeleton + route-level Suspense polish
- Internationalization (`react-i18next`, en + id)
- Dark mode (Tailwind `darkMode: "class"`)
- Test harness (Vitest + RTL + MSW)
- E2E coverage (Playwright)
- Accessibility audit (`axe`, aria labels, contrast)
- Error boundary + Sentry / GlitchTip
- Sport / position onboarding wizard
- Share session link (when BE issues share tokens)
