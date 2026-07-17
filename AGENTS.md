# Repository Guidelines

## Project Structure & Module Organization

OpenTV is a single-module Kotlin Android application. Root Kotlin DSL files configure the build, while dependency versions are centralized in `gradle/libs.versions.toml`.

- `app/src/main/java/com/lc5900/tv/data/`: channel models and raw-resource parsing.
- `app/src/main/java/com/lc5900/tv/ui/`: Compose screens, ViewModels, player UI, and theme.
- `app/src/main/res/raw/urls.txt`: bundled channel catalog.
- `app/src/test/`: JVM unit tests for data and state logic.
- `app/src/androidTest/`: device or emulator UI tests when needed.

Do not commit generated `build/`, `.gradle/`, `local.properties`, APK, or IDE workspace files.

## Build, Test, and Development Commands

Use JDK 17 or newer and Android SDK 36. Run the checked-in wrapper from the repository root; on macOS/Linux replace `gradlew.bat` with `./gradlew`.

- `gradlew.bat assembleDebug` builds the debug APK.
- `gradlew.bat installDebug` installs it on a connected target.
- `gradlew.bat testDebugUnitTest` runs JUnit tests.
- `gradlew.bat connectedDebugAndroidTest` runs instrumentation and Compose UI tests.
- `gradlew.bat lintDebug` checks Android, Compose, and resource issues.
- `gradlew.bat clean` removes generated outputs.

## Coding Style & Architecture

Use Kotlin with four-space indentation, trailing commas in multiline declarations, and idiomatic immutable data. Name types and composables with `UpperCamelCase`, functions and properties with `lowerCamelCase`, and constants with `UPPER_SNAKE_CASE`. Keep UI state in ViewModels as `StateFlow`; composables should render state and emit events rather than load data directly. Use Material 3 components and support light/dark themes, edge-to-edge layouts, rotation, and accessibility descriptions.

## Testing Guidelines

Place deterministic parsing and ViewModel tests in `app/src/test`; reserve `app/src/androidTest` for Android and Compose behavior. Name tests by behavior, such as `parseChannels_ignoresEntriesWithoutPlayableUrls`. Run unit tests and lint before every pull request, plus device tests for playback or lifecycle changes.

## Commit & Pull Request Guidelines

History uses short imperative summaries such as `use ijkplayer`. Keep commits focused and describe the observable change. Pull requests must summarize the change, list verification commands, and link related issues. Include screenshots for Compose changes and device/API details for Media3 playback changes.
