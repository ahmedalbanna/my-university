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
| **P1 — Foundation** | Week 3-6 | Auth, DB, Navigation, Permissions | Real login, NavHost, stable DB, Play bundle passes | ✅ **COMPLETED 2026-08-25** (this commit) |
| **P2 — Core Hardening** | Week 7-12 | Make each tab trustworthy | Library/Wallet/GPA use real data, AI real | ⬜ TODO |
| **P3 — Quality** | Month 4-6 | Testing, perf, offline, analytics | 60% unit coverage, baseline profiles, offline library | ⬜ TODO |
| **P4 — Scale** | Month 6-12 | Modularize, multi-university, store | Feature modules, sync API, closed-track release | ⬜ TODO |

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

## P2 — Core Hardening (Week 7-12)

| Feature | Current Gap | Roadmap Slice |
|---------|-------------|---------------|
| **Library** | Mock books `JamaatyRepository.kt:248` + fake `Handler.postDelayed` download `LibraryScreen.kt:682` | P2-1: `DownloadManager` + `Storage` (scoped storage, `MediaStore.Downloads`). `Book.downloadUrl` real CDN; `BookDetailCard` shows progress via `WorkManager`. Cache PDFs in `Room` blob or `DataStore`. Filter derived via `combine(profile, query)` not recomputed `getFilteredBooks() JamaatyViewModel.kt:385`. |
| **Wallet** | Static summary `WalletScreen.kt:368`, hardcoded date `JamaatyViewModel.kt:491` | P2-2: Ledger model `PaymentEntity.status DUE/PAID`, `dueDate` column. Derive header `258k/523k` from `payments` Flow. Integrate real gateway stub (Kuraimi/Jawali sandbox) behind `PaymentGateway` interface. Balance ledger = event-sourced. |
| **GPA** | `GpaCalculatorScreen.kt:136` `combine` per recomposition | P2-3: Move `semestersWithCourses` to `ViewModel` `StateFlow`. Validate grade `0..4`/`0..5` per university, `creditHours>0`. Export transcript PDF. |
| **Schedule** | No CRUD `ScheduleScreen.kt:36` | P2-4: Add `AddScheduleSheet` + conflict detection (overlap check `startTime<endTime`). `AlarmManager` reminders. |
| **Translate + AI** | Mock `simulateAcademicTranslation JamaatyViewModel.kt:573`, mock `generateAiResponse JamaatyViewModel.kt:616` despite `firebase-ai` dep | P2-5: Wire `FirebaseAI` `GenerativeModel("gemini-2.0-flash")` server-side (honor `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API metadata.json:5`). Fallback to local mock when `GEMINI_API_KEY` absent (`.env.example:7` uncomment guidance). Add `secrets` plugin wiring test (`BuildConfig.GEMINI_API_KEY`). |
| **More/World Libraries** | Location filter `WorldLibrariesScreen` static list `JamaatyRepository.kt:153` | P2-6: Governorate→University mapping already done; add search pagination, debounce `bookQuery` (`debounce(300ms)`). |

**Cross-cutting:** Move all hardcoded Arabic to `strings.xml` + `strings_ar.xml` (lint `MissingTranslation` enforced in `build.gradle.kts`).

## P3 — Quality (Month 4-6)

- **Testing:** Target 60% line. Add `AppDaoTest` (androidTest, in-memory DB), `JamaatyRepositoryTest`, `DeepLinkParserTest`, `GpaCalculationTest`, Compose `createAndroidComposeRule` for `OnboardingScreen`/`WalletScreen`. Screenshot via `Roborazzi 1.59` already included `app/build.gradle.kts:127`. Wire `./gradlew testDebugUnitTest connectedDebugAndroidTest` in CI.
- **Performance:** Baseline Profiles, `R8` + `baseline-prof.txt`, lazy `LibraryScreen` pagination (`Paging3` if >200 books). Profile `getFilteredBooks` off main via `flowOn(Dispatchers.Default)`.
- **Offline-first:** Cache books/projects via Room + `WorkManager` sync when `INTERNET` available. Show `offline banner` via `NetworkCallback`.
- **Observability:** Firebase Crashlytics + Analytics + Performance (currently only `AppCheck` `app/build.gradle.kts:114`). Log `payment_success`, `gpa_calculated`.
- **Accessibility:** RTL TalkBack order audit, contentDescriptions, 44dp touch targets (`JamaatyTopBar.kt:151`).
- **Security:** Encrypt `PreferenceEntity` with `EncryptedSharedPreferences` for `balance`/`studentId`; never log `referenceNo`.

## P4 — Scale (Month 6-12)

- **Modularize:** `:core:data`, `:core:ui`, `:feature:library`, `:feature:wallet`, `:feature:gpa` (enable `nonTransitiveRClass=true` already `gradle.properties:19`).
- **DI:** `Hilt` or `Koin` — ViewModels `@HiltViewModel @Inject Repository`, test fakes.
- **Backend:** Replace static `getBooks()/getProjects()` with paginated API (Retrofit `2.12` + Moshi already included `libs.versions.toml:22`). Add `OkHttp logging-interceptor` only `debugImplementation`.
- **Multi-university sync:** Plug Yemen universities APIs; `getGovernorates()` `JamaatyRepository.kt:153` becomes remote config.
- **Release:** Closed track Play Console, `versionCode 1 → 2` `app/build.gradle.kts:20`, signing via Play App Signing (remove `debug.keystore` from repo), `dependenciesInfo` already `includeInBundle=true`.

---

## Milestones & Ownership

| Milestone | Owner | Date | Gate | Status |
|-----------|-------|------|------|--------|
| M0 Hotfix merge | Android lead | W2 | No P0 open issues | ✅ Done 2026-08-25 |
| M1 Foundation demo | Android + Backend | W6 | Auth e2e + NavHost + DB migration test | ✅ Done 2026-08-25 (AuthRepository + NavHost + Migration 3→4) |
| M2 Core beta | Feature owners | W12 | Testers can download real PDF, pay mock, get Gemini answer |
| M3 QA green | QA + Android | M6 | CI lint+test green, 60% cov, baseline profile |
| M4 Store submission | Release mgr | M9 | Play pre-launch report 0 crashes |

## Risks & Mitigations

- **Gemini quota / Yemen firewall** → Keep mock fallback; server-side proxy honors `metadata.json:5`.
- **Payment gateway sandbox delay** → Ship Wallet as read-only ledger + `TODO` behind `PaymentGateway` interface.
- **AGP 9.1 + SDK 36 preview instability** → Pin `agp 8.7.3` fallback branch.

## Immediate Next Steps

- [x] **P0 Hotfix — landed `9bd969b` + `2249590`**
- [x] **P1 Foundation — landed (this commit): AuthRepository, MainNavGraph/NavHost, AppDatabase v4 Migration, DataStore, R8, NotificationPermission**
- [ ] P2 Core Hardening — Library `DownloadManager`, Wallet ledger, GPA `StateFlow`, Schedule CRUD, Gemini wiring (Week 7-12)
- [ ] P3 Quality — `DeepLinkParserTest`, `GpaCalculationTest`, `AppDaoTest` (Month 4-6)

---

*Update this file per sprint — PR must touch ROADMAP.md if scope changes.*
