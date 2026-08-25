# Modularization Plan — P4 Scale

> `nonTransitiveRClass=true` already enabled `gradle.properties:19`. Next step: extract `:core` and `:feature` modules.

## Target Graph

```
:core:data  (Room, DataStore, Retrofit, Moshi, SecureStorage)
  ├─ entities, dao, AppDatabase, UserPreferencesManager, JamaatyApiService, RemoteConfig
:core:ui    (Material3, Theme, OfflineBanner, JamaatyTopBar/BottomBar)
:feature:library (LibraryScreen + ViewModel filteredBooksFlow + DownloadManager)
:feature:wallet  (WalletScreen + PaymentGateway + ledger)
:feature:gpa     (GpaCalculatorScreen + calculateGpa)
:feature:schedule (ScheduleScreen + AddScheduleSheet)
:feature:translate (TranslateScreen + FirebaseAI)
:app        (NavHost, MainNavGraph, AppContainer DI)
```

## Current State (P4-1 Done)

- Logical packages already separated: `com.example.data.*`, `com.example.ui.*`, `com.example.di.AppContainer`
- `AppContainer` (manual DI) documents graph for future `@HiltViewModel` migration:
  ```kotlin
  @HiltViewModel class JamaatyViewModel @Inject constructor(
    private val repo: JamaatyRepository,
    private val authRepo: AuthRepository,
    private val api: JamaatyApiService
  ) : ViewModel()
  ```
- `settings.gradle.kts` ready for `include(":core:data", ":core:ui", ":feature:library", ...)` — uncomment when extracting.

## Migration Steps

1. Create `:core:data` module with `build.gradle.kts` (kotlin + room + retrofit + datastore)
2. Move `data/local`, `data/remote`, `data/repository` to `:core:data`
3. Create `:core:ui` for `Theme`, `OfflineBanner`, `JamaatyBottomBar`
4. Extract each `:feature:*` as `com.android.library` with Compose
5. Add `hilt` plugin `libs.plugins.hilt` + `ksp` when DI needed

## Why Not Yet Physical Modules?

Single-module keeps `assembleDebug` <5m on CI and avoids pre-merge build break. Logical separation + `AppContainer` already enforces boundaries checked by `DependencyGuard`.
