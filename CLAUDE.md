# LapLog-Android

Native Android port of the SwiftUI LapLog stopwatch. Kotlin + Jetpack Compose, single-activity / single-ViewModel architecture.

## Stack
- Compose BOM 2026.04.01, Material 3
- `androidx.lifecycle:lifecycle-viewmodel-compose` for `LapLogViewModel`
- `androidx.datastore:datastore-preferences` for persistence
- `kotlinx.serialization` (JSON) to encode the full app snapshot into a single Preferences string key
- `compileSdk` 36 (Android 16), `minSdk` 23, JDK 17

## Module layout
- `app/src/main/java/com/javierorraca/laplog/`
  - `MainActivity.kt` — `ComponentActivity` that hosts `LapLogApp` and owns the `LapLogViewModel`
  - `data/LapLogModels.kt` — `Lap`, `Session`, `AppTheme`, `LapLogSettings`, `LapLogSnapshot`, `quickPicks`
  - `data/LapLogRepository.kt` — wraps `preferencesDataStore("laplog")`, encodes/decodes the snapshot
  - `ui/LapLogViewModel.kt` — `AndroidViewModel` exposing `uiState: StateFlow<LapLogUiState>`, drives the ticker, archives sessions, persists on every state change via `updateAndPersist`
  - `ui/LapLogApp.kt` — top-level Compose UI (timer, controls, lap list, history/menu/settings/quick-pick bottom sheets)
  - `ui/Palette.kt`, `ui/TimeFormat.kt` — theme palettes and time formatting helpers

## Persistence model
Whole-app state is serialized as a single JSON blob into a `stringPreferencesKey("snapshot")` inside the `laplog` Preferences DataStore. `LapLogSnapshot` covers settings + history + the in-progress session (sans `running` flag — sessions always reload paused). Defaults on every persisted field, so old payloads decode cleanly when the schema grows. Reads use `kotlinx.serialization.Json { ignoreUnknownKeys = true; encodeDefaults = true }` so default-true booleans round-trip without flipping to false on missing keys.

## Conventions
- Mutations go through `LapLogViewModel.updateAndPersist { ... }` so every change is saved.
- New `Session`/`Lap` records use `UUID.randomUUID()` ids; serialized so renames survive restart.
- iOS sibling project lives at `../LapLog-iOS` — keep parity on user-visible features (themes, accent swatches, settings).

## Build & test
```
./gradlew test            # unit tests
./gradlew assembleDebug   # debug APK
```
Instrumentation: `LapLogSmokeTest` under `app/src/androidTest`.
