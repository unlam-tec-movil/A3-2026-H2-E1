# Technology Stack

## Architecture

Hexagonal (ports and adapters) on Android. Domain defines models and repository contracts; data layer implements them; UI depends on domain, never the reverse.

## Core Technologies

- **Language**: Kotlin (bytecode target Java 17; Gradle daemon toolchain requests JDK 25)
- **UI**: Jetpack Compose + Material 3 (Compose BOM)
- **DI**: Hilt (Dagger) via KSP
- **Build**: Gradle Kotlin DSL, single module `app`, versions centralized in `gradle/libs.versions.toml` (version catalog)
- **SDK**: minSdk 24, compileSdk/targetSdk 37

## Key Libraries

- Coil (image loading in Compose)
- Hilt Navigation Compose (`hiltViewModel()` in Screens)
- Kover (coverage), ktlint Gradle plugin (formatting)

## Development Standards

### Code Quality
- ktlint enforced via `.editorconfig`; `@Composable` functions may be PascalCase
- Four-space indent; guard clauses and small single-purpose functions
- Comments only for non-obvious logic

### Testing
- JVM tests: JUnit 4 in `app/src/test/`; device tests: AndroidX JUnit / Espresso / Compose test in `app/src/androidTest/`
- Test classes named `<Subject>Test`; run tests after every change
- Kover coverage published by CI; minimum currently 0%

## Development Environment

### Required Tools
- Android Studio with the ktlint plugin; Android SDK path in local `local.properties` (never commit)

### Common Commands
```bash
# Build:  ./gradlew :app:assembleDebug
# Test:   ./gradlew :app:testDebugUnitTest
# Lint:   ./gradlew :app:ktlintCheck :app:lint
# Format: ./gradlew :app:ktlintFormat
# Cover:  ./gradlew :app:koverXmlReportRelease
```

## Key Technical Decisions

- Domain must not depend on Hilt, Room, Retrofit, ViewModel, or LiveData; keep it plain Kotlin
- Domain models are business models (no persistence annotations); prefer behavior-bearing models over anemic ones
- Repository interface lives with the domain contract in this codebase; implementations (stubs, future network/local adapters) live in `data/`
- Single-activity app with Navigation Compose (per README guidance)

---
_Document standards and patterns, not every dependency_
_updated_at: 2026-11-10_
