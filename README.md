# LapLog-Android

LapLog is a minimalist stopwatch built around a single idea: every lap deserves a name. Tap any lap to rename it on the fly and turn anonymous splits into a real record. Sessions get titled and archived to a history view you can revisit later.

Native Android version of the SwiftUI LapLog prototype, built with Kotlin, Jetpack Compose, DataStore, and Android 16 SDK tooling.

## Features

- Editable current and completed lap names
- Sequential and parallel lap timing modes
- Reset-to-history session archive
- Editable history sessions and lap names
- Quick-pick lap names on long press
- Warm light, Paper, and Dark themes
- Accent swatches with theme-aware monochrome accent
- Big numerals toggle
- Clipboard export
- DataStore persistence for current state, settings, and history

## Build

Requires Android Studio Panda 4 / 2025.3.x or newer, JDK 17, and the Android 16 SDK.

```bash
./gradlew test
./gradlew assembleDebug
```

The app uses:

- `compileSdk` Android 16 API 36.1
- `targetSdk` Android 16 API 36
- `minSdk` API 23
- Application ID `com.javierorraca.laplog`
