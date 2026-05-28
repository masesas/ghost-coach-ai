# Ghost Coach Mobile

Flutter mobile client for Ghost Coach — upload a stance photo, get AI coaching feedback, chat about it.
Mirrors the web app and consumes the same backend API.

---

## Stack

| Layer | Choice |
|---|---|
| Runtime | Flutter 3.38.x · Dart SDK ^3.10 |
| State | `flutter_bloc` 9 + `equatable` |
| Routing | `go_router` 17 + `go_router_builder` (codegen, type-safe routes) |
| DI | `get_it` 9 |
| Network | `dio` 5 + `chucker_flutter` (dev / staging inspector) |
| Storage | `shared_preferences` |
| Media | `image_picker`, `cached_network_image` |
| Permissions | `permission_handler` |
| Charts | `fl_chart` |
| UX | `fluttertoast`, `logger`, `intl` |
| Branding | `flutter_launcher_icons`, `flutter_native_splash` |
| Flavors | `main_dev.dart` / `main_staging.dart` / `main_prod.dart` |

---

## Prerequisites

- Flutter 3.38+ (`flutter doctor` clean for your target platform)
- Android SDK / Xcode for the platform you build
- Backend running (see `../ghost-coach-be/README.md`)

## Setup

```bash
flutter pub get
dart run build_runner build --delete-conflicting-outputs   # generate go_router routes
```

## Run

Default base URLs per flavor (`lib/core/env/app_env.dart`):

| Flavor | `baseUrl` | Chucker inspector |
|---|---|---|
| `dev` | `http://10.0.2.2:8080/api/v1` (Android emulator → host) | on |
| `staging` | `https://staging.ghostcoach.example.com/api/v1` | on |
| `prod` | `https://api.ghostcoach.example.com/api/v1` | off |

```bash
make run-dev          # flutter run -t lib/main_dev.dart
make run-staging      # flutter run -t lib/main_staging.dart
make run-prod         # flutter run -t lib/main_prod.dart --release
```

iOS Simulator or a physical device needs an explicit override (the emulator-only `10.0.2.2` won't resolve):

```bash
flutter run -t lib/main_dev.dart \
  --dart-define=API_BASE_URL=http://192.168.1.10:8080/api/v1
```

### Makefile Shortcuts

| Target | Purpose |
|---|---|
| `make get` | `flutter pub get` |
| `make build-runner` / `make watch` | One-shot / watch codegen |
| `make analyze` | `flutter analyze` |
| `make format` | `dart format lib/ test/` |
| `make clean` | `flutter clean && flutter pub get` |
| `make apk-prod` | Build release APK from `main_prod.dart` |

iOS release (no Makefile target):

```bash
flutter build ios -t lib/main_prod.dart --release
```

### Routes

Codegen via `@TypedGoRoute` in `lib/common/router/app_routes.dart` → `app_routes.g.dart`.

| Path | Page | Notes |
|---|---|---|
| `/login`, `/register` | `LoginPage`, `RegisterPage` | Public; redirect to `/upload` if a token is present |
| `/upload` | `UploadPage` | Inside `ShellPage` — bottom-nav scaffold |
| `/history` | `HistoryPage` | Paginated session list + progress chart |
| `/history/:id` | `SessionDetailPage` | Feedback report + chat panel |

Auth-driven redirects are wired through `auth_refresh_stream.dart` so the router reacts to token changes.

### Project Layout

```
lib/
├── core/
│   ├── env/         # AppEnv enum (per-flavor baseUrl, chucker flag)
│   ├── network/     # Dio instance + interceptors (auth, error, chucker)
│   ├── storage/     # SharedPreferences-backed token store
│   ├── di/          # get_it registrations
│   └── extension/   # Dart/Flutter extension methods
├── common/
│   ├── theme/       # ThemeData (light only today)
│   ├── router/      # GoRouter setup, typed routes, auth refresh stream
│   ├── widgets/     # Reusable widgets (cards, badges, states)
│   └── util/        # Formatting, validators
├── data/
│   ├── model/       # JSON models (manual fromJson/toJson, no codegen for models)
│   ├── source/      # Remote sources (Dio calls)
│   └── repository/  # Repositories consumed by blocs
└── feature/
    ├── shell/       # ShellPage (bottom-nav)
    ├── auth/        # login + register (page / widget / bloc)
    ├── upload/      # ImagePicker → multipart POST
    ├── history/     # Paginated list + progress chart
    ├── detail/      # Feedback report
    └── chat/        # Coaching chat per session

main_dev.dart · main_staging.dart · main_prod.dart   # flavor entrypoints
```

Implementation plans for each layer live in [`docs/impl-plans/`](docs/impl-plans/) (`00-overview.md` → `09-release-build.md`).

---

## Key Mobile Decisions

| Area | Decision | Rationale |
|---|---|---|
| **State management** | `flutter_bloc` with `Equatable` states / events; one bloc per feature. | Predictable async flow, easy to test (`bloc_test` later), no rebuilds on equal state. |
| **Routing** | `go_router` + `go_router_builder` codegen with `@TypedGoRoute`. | Compile-time path / param safety; refactors don't silently break navigation. |
| **Auth-driven redirects** | `auth_refresh_stream.dart` exposes a stream the `GoRouter.refreshListenable` watches. | Login / logout updates routing without manual `context.go(...)` everywhere. |
| **Flavors** | Three entrypoints (`main_dev` / `main_staging` / `main_prod`) feed an `AppEnv` enum; `--dart-define=API_BASE_URL=…` overrides at runtime. | Reproducible builds per environment; LAN / device overrides without touching code. |
| **Networking** | Single `Dio` instance built in `core/network`; bearer token attached via interceptor; `chucker_flutter` enabled only when `AppEnv.showChucker` is true. | One config surface; in-app traffic inspector in dev/staging, zero overhead in prod. |
| **Token storage** | `shared_preferences` (per MVP spec, **not encrypted**). | Trade-off documented in `Known Limitations`; move to `flutter_secure_storage` before any real launch. |
| **Authenticated images** | `cached_network_image` with the auth header injected via the Dio interceptor (re-using the same client). | Disk cache for free; mirrors the web app's "no signed URLs" decision. |
| **DI** | `get_it` registered in `core/di`; blocs constructed against repository interfaces. | Constructor injection for testability; easy to swap fakes. |
| **Image capture** | `image_picker` with `ImageSource.camera` and `ImageSource.gallery`; size validated client-side at ≤ 5 MB to match `APP_MAX_FILE_SIZE` on the backend. | Same hard cap on both sides; avoids a wasted multipart round-trip. |
| **Chart** | `fl_chart` `LineChart` in History — same data shape (`SessionSummary` list) as the web `ProgressChart`. | One mental model across web + mobile. |
| **Theme** | Single light Material 3 theme today; dark mode pending. | Listed in Next Steps. |
| **Codegen** | Only `go_router_builder` (routes), `flutter_launcher_icons`, `flutter_native_splash`. Models are hand-written `fromJson`. | Avoids `json_serializable` rebuild cost for a small DTO surface. Will revisit if the model count grows. |

---

## Known Limitations (current MVP)

- No offline mode — every screen needs the BE.
- No visual annotation on the feedback image (BE doesn't return coordinates yet).
- JWT in `shared_preferences`, **not encrypted**.
- No automated tests yet — manual QA only.
- Light theme only.
- iOS push notifications not configured.

---

### Next Steps Planned

1. **Quota-aware UX (mirrors BE free-tier enforcement).** _Why first: the BE plan ships the cap; the app must not look broken when it hits._
   When the BE returns `HTTP 429 QUOTA_EXCEEDED`, show a `QuotaSheet` (bottom sheet) on `/upload` and disable the chat input on `/history/:id`. Pull `limit`, `used`, `resetAt` from the response payload — never compute client-side.
   - Touches: a `UsageBloc`, a new `quota_sheet.dart` in `common/widgets`, and one new interceptor branch.
   - Effort: S · Risk: low · Depends on: BE plan `16-free-tier-quota.md`.

2. **In-app camera capture flow with pose-guideline overlay.** _Why: single biggest UX upgrade for first-time stance uploads — most drop-offs happen at "pick an image"._
   Replace the `image_picker` handoff with a dedicated `CameraPage` built on the `camera` package: live preview, capture button, retake, and a translucent silhouette overlay per sport / position pulled from `/system-vars`.
   - Touches: new `feature/capture/`, permission flow via `permission_handler`, route under the home shell.
   - Effort: M · Risk: medium (camera lifecycle on Android quirks) · Depends on: nothing.

3. **On-device pose preview (Google ML Kit Pose Detection).** _Why: turns "blurry / cropped photo" rejections from a server `HTTP 422` into a client-side hint before upload — saves a round-trip and feels magical._
   Run pose detection on the captured frame (or live preview), highlight detected joints, and require a minimum confidence threshold + "full body in frame" check before enabling the submit button.
   - Touches: `google_mlkit_pose_detection` plugin, a `PoseOverlay` painter, gating logic in the `CapturePage` bloc.
   - Effort: M · Risk: medium (model size + cold-start latency) · Depends on: #2.

4. **Push notifications (FCM + APNs).** _Why: closes the loop with the BE's async analysis work and re-engages users (streak reminders, "new feedback ready")._
   Wire Firebase Messaging on Android + APNs on iOS, deliver a push when the BE emits `feedback_ready` for a session, and deep-link to `/history/:id` via `go_router`.
   - Touches: `firebase_core` + `firebase_messaging`, native config (`google-services.json`, iOS entitlements), a `NotificationsBloc`, deep-link handler in `app_router.dart`.
   - Effort: M · Risk: medium (iOS provisioning + APNs cert) · Depends on: BE async-analysis work.

5. **Secure token storage.** _Why: must-have before any public release — `shared_preferences` is plaintext on disk._
   Replace the `TokenStore` implementation with `flutter_secure_storage` (Keychain on iOS, Keystore on Android). Single-file swap behind the existing interface; existing tokens migrated lazily on next launch.
   - Touches: `core/storage/token_store.dart`, one-time migration helper, DI registration.
   - Effort: S · Risk: low · Depends on: nothing.

> Implementation plans will land under `docs/impl-plans/` continuing from `10-`.

### Parked (Not Now, But Considered)

Kept here so we don't re-debate them every sprint. Re-evaluate after the 5 Planned items ship.

- Offline-first history (Drift / Isar)
- Streaming chat (SSE)
- Dark theme + Material You dynamic color
- Internationalization (en + id)
- Test harness (`bloc_test`, golden, integration)
- CI/CD (Codemagic / GitHub Actions + Fastlane)
- Sentry / Crashlytics
- AAB + asset-size diet
- Biometric unlock (`local_auth`)
- Accessibility pass
- Multi-frame video stance
- Share session (`share_plus`)
- Dedicated `/progress` tab
- Dev/staging in-app bug-report shortcut

---

## License

MIT
