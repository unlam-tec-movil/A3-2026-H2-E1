# Project Structure

## Organization Philosophy

Layered, hexagonal: `domain` (core) ← `data` (adapters) and `ui` (driving side). Intended to grow one package per business domain within each layer. Where the README's examples and the source layout differ, follow the source layout.

## Directory Patterns

### Domain
**Location**: `app/src/main/java/ar/edu/unlam/scaffoldingandroid3/domain/`
**Purpose**: Business models, repository contracts (ports), use cases/services. Pure Kotlin.
**Example**: `Book`, `BookRepository`, `BookService`

### Data
**Location**: `.../scaffoldingandroid3/data/`
**Purpose**: Repository implementations and adapters to external sources (optional `network/`, `local/` subpackages, DI module)
**Example**: `BookStubRepo` implements `BookRepository`; returns domain models, not data-layer models

### UI
**Location**: `.../scaffoldingandroid3/ui/`
**Purpose**: Container/component paradigm
- `components/`: stateless visual composables that receive state
- `screens/` (when added): container composables paired with a ViewModel, obtained via `hiltViewModel()`
- `theme/`: Color, Type, Theme

### Resources and Tests
- Resources: `app/src/main/res/`
- JVM tests: `app/src/test/`; device tests: `app/src/androidTest/` (mirror the main package path)

## Naming Conventions

- **Files/Classes**: PascalCase, filename matches the primary class
- **Functions/Properties**: camelCase (Composables PascalCase)
- **Packages**: lowercase
- **Tests**: `<Subject>Test`

## Import Organization

```kotlin
import androidx.compose.material3.Text                          // libraries first
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ScaffoldingAndroid3Theme  // project, absolute
```

No path aliases; absolute fully qualified imports, ordered by ktlint.

## Code Organization Principles

- Dependencies point inward: ui/data → domain; domain imports nothing Android
- Single activity (`MainActivity`) hosts theme, scaffold, and navigation
- Reusable composables go in `ui/components/`, not in `MainActivity`
- New domain = parallel package set across domain/data/ui

---
_Document patterns, not file trees. New files following patterns shouldn't require updates_
_updated_at: 2026-11-10_
