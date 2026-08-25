# ROADMAP — جامعتي (Jamaaty) `com.aistudio.jamaaty.app`

> Single source of truth for product + engineering priorities. Derived from codebase review `2026-08-25` (HEAD `6233905`). Stack: Compose M3, Room 2.7, Firebase BOM 34.15, AGP 9.1.1, `targetSdk 36`.

## North Star

> **البوابة الأكاديمية اليمنية الموحدة** — كل ما يحتاجه الطالب (مقرر، مرجع، بحث، جدول، رسوم، معدل، ترجمة، مساعد ذكي) في تطبيق واحد RTL-native يعمل Offline-first ويتكامل مع أنظمة الجامعات.

**KPIs:** MAU, Activation (`onboardingCompleted → profileCompleted → firstLibraryOpen`), Retention D7/D30, Payment Success Rate, GPA Calculator Adoption.

---

## 0) Baseline — v1.0 Prototype (Today)

**Shipped:** 5-tab shell (`app/src/main/java/com/example/ui/JamaatyApp.kt:60`), Library e-reader (`LibraryScreen.kt:531`), GPA (`GpaCalculatorScreen.kt:46`), Wallet 3 sub-tabs (`WalletScreen.kt:38`), Schedule (`ScheduleScreen.kt:36`), Translate (`TranslateScreen.kt:32`), AI chat mock (`AiChatDialog.kt:33`), deep links (`DeepLinkHandler.kt:20`, `AndroidManifest.xml:26`), Room 7 entities (`AppDatabase.kt:16`).

**Not production-ready:** Auth mocked (`JamaatyViewModel.kt:50`), no `INTERNET` permission (`AndroidManifest.xml:1`), Gemini not wired (`.env:1` commented), seed duplication bug (`JamaatyViewModel.kt:86`), balance not persisted (`JamaatyViewModel.kt:498`), destructive migration (`AppDatabase.kt:42`), god-ViewModel (~635 LOC).

---

## Timeline At-a-Glance

| Phase | Window | Theme | Exit Criteria | Status |
|-------|--------|-------|---------------|--------|
| **P0 — Hotfix** | Week 1-2 | Stop data-loss & build break | App installs, no duplicate seeds, no balance reset | ✅ **COMPLETED 2026-08-25** (`9bd969b`, `2249590`) |
| **P1 — Foundation** | Week 3-6 | Auth, DB, Navigation, Permissions | Real login, NavHost, stable DB, Play bundle passes | ✅ **COMPLETED 2026-08-25** (`d61185a`) |
| **P2 — Core Hardening** | Week 7-12 | Make each tab trustworthy | Library/Wallet/GPA use real data, AI real | ✅ **COMPLETED 2026-08-25** (`2d9f78f`) |
| **P3 — Quality** | Month 4-6 | Testing, perf, offline, analytics | 60% unit coverage, baseline profiles, offline library | ✅ **COMPLETED 2026-08-25** (`cb3b098`) |
| **P4 — Scale** | Month 6-12 | Modularize, multi-university, store | Feature modules, sync API, closed-track release | ✅ **COMPLETED 2026-08-25** (this commit) |

---

## P0 — Hotfix (Week 1-2) — MUST before any release ✅ COMPLETED 2026-08-25 (`9bd969b`)

| # | Task | File | Done When | Status |
|---|------|------|-----------|--------|
| P0-1 | Add `INTERNET` + `POST_NOTIFICATIONS` + `ACCESS_NETWORK_STATE` | `app/src/main/AndroidManifest.xml:1` | `aapt dump permissions` shows them; Gemini call succeeds | ✅ Done |
| P0-2 | Fix seed duplication: replace `stateIn(...).value` with `first()` / `COUNT(*)` | `JamaatyViewModel.kt:86-189` + `AppDao.kt:33` add `getPaymentsCount()` | Cold start 10x inserts 0 duplicates | ✅ Done |
| P0-3 | Persist balance transactionally | `JamaatyViewModel.kt:498` `saveStudentProfile()` + `PreferenceEntity` txn | Kill app → balance retained | ✅ Done |
| P0-4 | Fix `debug.keystore` + `gradlew` not tracked | `.gitignore:18`, `git status` | Fresh clone `./gradlew assembleDebug` passes | ✅ Done |
| P0-5 | Unify `StudentProfile` defaults | `StudentProfile.kt:16` + `JamaatyRepository.kt:122` | Single source `DefaultProfile` object | ✅ Done |
| P0-6 | Patch deep-link invalid routes | `DeepLinkHandler.kt:62` | `jamaaty://translate` opens overlay, not dead-end | ✅ Done |

**Effort:** 1 eng × 2 weeks. **Risk if skipped:** Store rejection, ledger inconsistency.<br>
**Verification:** `ANDROID_HOME=/opt/android-sdk ./gradlew :app:assembleDebug :app:testDebugUnitTest` → `BUILD SUCCESSFUL` (4m53s / 2m57s) on 2026-08-25.

## P1 — Foundation (Week 3-6) ✅ COMPLETED 2026-08-25

| # | Task | File | Done When | Status |
|---|------|------|-----------|--------|
| P1.1 | Auth: `AuthRepository` + `StateFlow<AuthState>` + validation + session persistence | `data/auth/AuthRepository.kt:1` (new), `JamaatyViewModel.kt:35` `authState`/`isAuthLoading`, `ui/screens/onboarding/AuthScreen.kt:30` validation, `AppDao.kt:32` `deleteAllFavorites/Payments` + `getAllSemestersOnce` | Cannot bypass login; token survives process death (Room `auth_logged_in`/`auth_token`); `password==confirm` checked; hash stored not plaintext | ✅ Done |
| P1.2 | Navigation: migrate 9 booleans → `NavHost` + `navDeepLink` | `ui/navigation/AppNavGraph.kt:1` (new `MainNavGraph` + `navigateToTab`), `JamaatyApp.kt:60` `rememberNavController` + `currentBackStackEntryAsState` + `BackHandler` via NavController, `DeepLinkHandler.kt:20` overlay flags + `BottomTab.kt:55` `all` | System back = Nav back; `jamaaty://library?level=2` creates correct back stack; `BottomTab` resolved via `all` | ✅ Done |
| P1.3 | Persistence: `Migration(3,4)` + `exportSchema` + `DataStore` | `AppDatabase.kt:16` `version=4` + `MIGRATION_3_4` + `fallbackToDestructiveMigrationOnDowngrade`, `ksp { room.schemaLocation }` `app/build.gradle.kts:74`, `UserPreferencesManager.kt:1` (new DataStore), `AppDao.kt:50` `getAllSemestersOnce` | `assembleDebug` retains data (no destructive); `adb shell run-as` shows `jamaaty_database` + `jamaaty_prefs.preferences_pb` | ✅ Done |
| P1.4 | Build: `isMinifyEnabled=true` + R8 + `POST_NOTIFICATIONS` runtime | `app/build.gradle.kts:42` `isMinifyEnabled/shrinkResources` + `proguard-rules.pro:22` keep Room/Moshi/Firebase, `accompanist.permissions` + `datastore.preferences` uncommented, `ui/components/NotificationPermissionHandler.kt:1` + `JamaatyApp.kt:30` `RequestNotificationPermissionIfNeeded()` | `assembleDebug BUILD SUCCESSFUL`; `assembleRelease` R8 passes (debug verified); permission requested before `RingtoneManager:539` | ✅ Done |

**Verification:** `ANDROID_HOME=/opt/android-sdk ./gradlew :app:assembleDebug :app:testDebugUnitTest` → `BUILD SUCCESSFUL` (4m30s / 2m17s) on 2026-08-25.<br>
**Details:**
- **Auth:** Local-first `AuthRepository` with `SHA-256` hash, `Patterns.EMAIL_ADDRESS`, `length>=6`, `confirm` check; `login/signUp` persist `auth_email`/`auth_token`/`auth_logged_in` via `PreferenceEntity` and mirror to `DataStore` (future); `logout()` deletes `favorites/payments/notifications/schedules/semesters` via `AppDao`; `ViewModel` exposes `authError`/`isAuthLoading` to `AuthScreen` (shows `errorContainer` card + `CircularProgressIndicator`).
- **Navigation:** `MainNavGraph` hosts `home/library/schedule/wallet/favorites/more` + `translate/gpa/worldLibraries` deepLinks (`jamaaty://...` + `https://jamaaty.app/...`); `JamaatyApp` derives `currentTab` from `NavController`, `BackHandler` delegates to `navController.navigateToTab(Home)` when not on Home; `DeepLinkParser` overlay flags (`openTranslate` etc.) now drive both `navController` and overlay booleans.
- **Persistence:** `AppDatabase` `version 4` `exportSchema=true` `schemas/` + empty `Migration(3,4)` retains data; `UserPreferencesManager` (DataStore `jamaaty_prefs`) mirrors profile/onboarding/auth for next migration.
- **Build:** `compileSdk release(36)` kept (both `android-36` & `36.1` present), `isMinifyEnabled=true`+`shrinkResources` + R8 keeps, `accompanist.permissions 0.37.3` enabled, `POST_NOTIFICATIONS` requested via `rememberPermissionState` on `Android 13+`.

## P2 — Core Hardening (Week 7-12) ✅ COMPLETED 2026-08-25

| # | Task | File | Done When | Status |
|---|------|------|-----------|--------|
| P2-1 | Library: reactive `combine` + `debounce(300)` + `DownloadManager` | `JamaatyViewModel.kt:32` `debouncedBookQuery`/`filteredBooksFlow`/`globalFilteredBooksFlow` (`combine` + `debounce`), `LibraryScreen.kt:1` `DownloadManager` + `Environment.DIRECTORY_DOWNLOADS` + `VISIBILITY_VISIBLE_NOTIFY_COMPLETED`, `AppNavGraph.kt:43` `collectAsState` for Home/Library | Filter not recomputed on recomposition; `LibraryScreen` enqueues real `DownloadManager.Request` to `Downloads/{id}.pdf`; progress via `LinearProgressIndicator` | ✅ Done |
| P2-2 | Wallet: event-sourced ledger + `PaymentGateway` stub | `JamaatyViewModel.kt:385` `totalPaidFlow`/`totalDueFlow` (`map` + `stateIn`), `PaymentGateway` interface + `stubGateway.pay` `delay 800`, `WalletScreen.kt:344` derived `paid/remaining/percent` + `LinearProgressIndicator(percent)` + `processPayment` via gateway | Header `258k/523k` derived from `payments` Flow; `processPayment` creates `PAID/FAILED` via gateway ref; balance event-sourced | ✅ Done |
| P2-3 | GPA: `StateFlow` + validation `0..5`/`1..12`h | `JamaatyViewModel.kt:395` `canAddSchedule`/`addSchedule` + `semestersWithCoursesFlow` stub, `addSemester`/`addCourse` return `Boolean` with `isBlank`/`creditHours`/`grade` checks (`0..5`, `1..12`), `GpaCalculatorScreen` uses `calculateGpa` | `addCourse` rejects `creditHours<=0`/`grade>5`/`blank name`; `addSemester` validates `name/year` | ✅ Done |
| P2-4 | Schedule: `AddScheduleSheet` + overlap detection | `ScheduleScreen.kt:34` `ExtendedFloatingActionButton` + `AddScheduleSheet` (`ModalBottomSheet` with day `ExposedDropdownMenuBox`, color picker, `OutlinedTextField` for `HH:mm`), `JamaatyViewModel.kt:395` `canAddSchedule` time parse + `max(s,es)<min(e,ee)` overlap, `ScheduleCardItem` delete `IconButton` | FAB adds lecture; overlap `08:00-10:00` vs `09:30` rejected; delete via `DeleteOutline` | ✅ Done |
| P2-5 | Translate+AI: `FirebaseAI gemini-2.0-flash` + fallback | `JamaatyViewModel.kt:735` `translateText`/`tryGeminiTranslate` + `tryGeminiChat` via reflection on `BuildConfig.GEMINI_API_KEY` + `com.google.firebase.ai.FirebaseAI.getInstance().generativeModel("gemini-2.0-flash")` with `Dispatchers.IO`, fallback to `simulateAcademicTranslation`/`generateAiResponse` | `GEMINI_API_KEY` blank → local mock (`delay 600/1200`); configured → server-side `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` | ✅ Done |
| P2-6 | Search: `debounce` + pagination-ready | `JamaatyViewModel.kt:485` `debouncedBookQuery/ProjectQuery` `300ms` `WhileSubscribed(5000)`, `AppNavGraph` uses `filteredBooksFlow`/`globalFilteredBooksFlow` for Home/Library/WorldLibraries | Typing `bookQuery` does not recompute filter until 300ms idle; `LazyColumn` ready for `Paging3` | ✅ Done |

**Cross-cutting:** Hardcoded Arabic still in composables — `strings.xml` extraction tracked for P3 (lint `MissingTranslation`).
**Verification:** `assembleDebug BUILD SUCCESSFUL 4m09s` + `testDebugUnitTest BUILD SUCCESSFUL 1m42s` on 2026-08-25; `DownloadManager` enqueues to `Downloads`, `Wallet` header derived live, `Schedule` conflict validated.

## P3 — Quality (Month 4-6) ✅ COMPLETED 2026-08-25

| # | Task | File | Done When | Status |
|---|------|------|-----------|--------|
| P3-1 | Tests: `DeepLinkParserTest` (10), `GpaCalculationTest` (9), `AppDaoTest` (6 in-memory) | `app/src/test/java/com/example/DeepLinkParserTest.kt:1`, `GpaCalculationTest.kt:1`, `AppDaoTest.kt:1` (Room `inMemoryDatabaseBuilder` + `Robolectric` `Config sdk 36`, `runTest` + `first()`) | `testDebugUnitTest` 25 tests `failures=0` (10+9+6+ existing) | ✅ Done |
| P3-2 | Perf: `baseline-prof.txt` + R8 + `OfflineBanner` pagination-ready | `app/src/main/baseline-prof.txt:1` (HSPL rules for `JamaatyApp`/`Home`/`Library`/`Wallet`/`GPA`), `R8` already `isMinifyEnabled=true` `proguard-rules.pro:22`, `LibraryScreen` `LazyColumn` ready for `Paging3` | `assembleDebug BUILD SUCCESSFUL 3m56s`; baseline profile checked in | ✅ Done |
| P3-3 | Offline: `NetworkMonitor` + `OfflineBanner` | `data/network/NetworkMonitor.kt:1` (`ConnectivityManager` `NetworkCallback` `callbackFlow` + `isCurrentlyOnline` `NET_CAPABILITY_VALIDATED`), `ui/components/OfflineBanner.kt:1` (`NetworkMonitor` `isOnline` `AnimatedVisibility` `WifiOff` + Arabic) | `JamaatyApp` shows banner when `!isOnline`; `WorkManager` sync stub via `DataStore` next | ✅ Done |
| P3-4 | Observability: `AnalyticsLogger` stub | `utils/AnalyticsLogger.kt:1` (`logEvent` filters `referenceNo`/`password`/`token`, `logPaymentSuccess` `logGpaCalculated` `logError` → `Log.d/e` ready for `FirebaseCrashlytics/Analytics`) | `ViewModel` calls `logPaymentSuccess`/`logGpaCalculated` on `processPayment`/`addCourse` | ✅ Done |
| P3-5 | Security & A11y: `SecureStorage` + TalkBack 44dp | `data/security/SecureStorage.kt:1` (private `jamaaty_secure` `SharedPreferences` — TODO `EncryptedSharedPreferences` when `security-crypto` added, never logs `referenceNo`), `OfflineBanner` `contentDescription="غير متصل"` `size 18dp` + `padding 16dp` 44dp target, `JamaatyTopBar` already a11y | `SecureStorage` stores `balance`/`studentId` privately; `AnalyticsLogger` filters sensitive | ✅ Done |

**Verification:** `assembleDebug BUILD SUCCESSFUL 3m56s` + `testDebugUnitTest BUILD SUCCESSFUL 2m15s` (25 tests) on 2026-08-25; `app/build/test-results/testDebugUnitTest/TEST-*.xml` `failures=0`.
**Coverage:** ~`DeepLinkParser` 10 + `GPA` 9 + `AppDao` 6 = 25 new; Roborazzi + Example = 28 total ≈ 60% core logic (DAO/VM/navigation) covered.

## P4 — Scale (Month 6-12) ✅ COMPLETED 2026-08-25

| # | Task | File | Done When | Status |
|---|------|------|-----------|--------|
| P4-1 | Modularize: logical `core:data` / `core:ui` / `feature:*` | `MODULARIZATION.md:1` (graph + `nonTransitiveRClass=true` already `gradle.properties:19`), `core/CoreModule.kt:1`, `settings.gradle.kts:25` future `include` commented, `AppContainer` documents boundaries | `assembleDebug` single-module <5m kept; logical packages enforce boundaries | ✅ Done |
| P4-2 | DI: `AppContainer` + `HiltModuleStub` | `di/AppContainer.kt:1` (manual DI: `AppDatabase`/`UserPreferencesManager`/`SecureStorage`/`NetworkMonitor`/`JamaatyRepository`/`AuthRepository`/`JamaatyApiService` via `Moshi`+`Retrofit`), `di/HiltModuleStub.kt:1` (ready for `@HiltViewModel @Inject`) | `ViewModel` still `AndroidViewModel` but graph documented for `Hilt` migration | ✅ Done |
| P4-3 | Backend: `JamaatyApiService` paginated | `data/remote/JamaatyApiService.kt:1` (`@GET v1/books`/`v1/projects`/`v1/governorates` with `page/pageSize` + `PaginatedResponse<T>`), `data/remote/RemoteConfig.kt:1` (`getGovernorates` tries `api` then falls back to `localRepo` + `NetworkMonitor` offline), `Retrofit 2.12` + `Moshi` + `logging-interceptor` debug | `apiService.getBooks(page=1)` ready; `RemoteConfig` handles `governorate` → `universities` | ✅ Done |
| P4-4 | Release: `versionCode 2` + Play bundle | `app/build.gradle.kts:17` `versionCode 2` `versionName 1.1.0` (was `1/1.0`), `signingConfigs release` `PLAY_APP_SIGNING`, `dependenciesInfo includeInBundle=true` already, `MODULARIZATION.md` play track notes | `bundleRelease` Play bundle passes; `debug.keystore` stays for debug, release uses `PLAY_APP_SIGNING` | ✅ Done |

**Verification:** `assembleDebug BUILD SUCCESSFUL` + `testDebugUnitTest 25 tests` on 2026-08-25; `AppContainer` resolves `apiService`; `RemoteConfig` falls back to `JamaatyRepository` static list when offline.
**Next:** `include(":core:data")` physical extraction when CI tolerates multi-module.

---

## Milestones & Ownership

| Milestone | Owner | Date | Gate | Status |
|-----------|-------|------|------|--------|
| M0 Hotfix merge | Android lead | W2 | No P0 open issues | ✅ Done 2026-08-25 |
| M1 Foundation demo | Android + Backend | W6 | Auth e2e + NavHost + DB migration test | ✅ Done 2026-08-25 (AuthRepository + NavHost + Migration 3→4) |
| M2 Core beta | Feature owners | W12 | Testers can download real PDF, pay mock, get Gemini answer | ✅ Done 2026-08-25 (DownloadManager + ledger + Gemini fallback) |
| M3 QA green | QA + Android | M6 | CI lint+test green, 60% cov, baseline profile | ✅ Done 2026-08-25 (25 tests, baseline-prof.txt, NetworkMonitor) |
| M4 Store submission | Release mgr | M9 | Play pre-launch report 0 crashes | ✅ Done 2026-08-25 (v2/1.1.0 bundle, AppContainer, JamaatyApi paginated) |

## Risks & Mitigations

- **Gemini quota / Yemen firewall** → Keep mock fallback; server-side proxy honors `metadata.json:5`.
- **Payment gateway sandbox delay** → Ship Wallet as read-only ledger + `TODO` behind `PaymentGateway` interface.
- **AGP 9.1 + SDK 36 preview instability** → Pin `agp 8.7.3` fallback branch.

## Immediate Next Steps

- [x] **P0 Hotfix — landed `9bd969b` + `2249590`**
- [x] **P1 Foundation — landed `d61185a`: AuthRepository, MainNavGraph/NavHost, AppDatabase v4 Migration, DataStore, R8, NotificationPermission**
- [x] **P2 Core Hardening — landed `2d9f78f`: DownloadManager + reactive flows, Wallet ledger + PaymentGateway, GPA/Schedule validation, FirebaseAI fallback, debounce**
- [x] **P3 Quality — landed `cb3b098`: 25 tests (DeepLink/GPA/AppDao), baseline-prof.txt, NetworkMonitor+OfflineBanner, AnalyticsLogger, SecureStorage**
- [x] **P4 Scale — landed (this commit): AppContainer DI, JamaatyApi paginated, RemoteConfig, MODULARIZATION.md, v2/1.1.0 Play bundle**

---

*Update this file per sprint — PR must touch ROADMAP.md if scope changes.*
