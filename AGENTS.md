# AGENTS.md

This file guides agentic coding assistants working in this repository.
Follow these instructions before changing code or running commands.

## Repository context
- Kotlin, multi-module Android app using Jetpack Compose.
- Clean Architecture with data, domain, presentation layers.
- MVI pattern in the UI layer with events, state, side effects.

## Local setup and secrets
- Add `TMDB_API_KEY=...` to `local.properties` (not committed).
- Do not commit API keys or `local.properties`.

## Cursor/Copilot rules
- No `.cursor/rules/`, `.cursorrules`, or `.github/copilot-instructions.md` found.

## Build commands
- `./gradlew assembleDebug` (build debug APK).
- `./gradlew build` (full build, unit tests, lint as configured).
- `./gradlew :app:assembleDebug` (app module only).

## Lint and formatting
- `./gradlew lint` (all modules, warnings as errors).
- `./gradlew :app:lintDebug` (app lint only).
- `./gradlew spotlessCheck` (ktlint + formatting checks).
- `./gradlew spotlessApply` (auto-format Kotlin/KTS/XML).

## Unit tests
- `./gradlew test` (all unit tests).
- `./gradlew :feature:movie-catalog:testDebugUnitTest` (module unit tests).

Run a single unit test
- `./gradlew :feature:movie-catalog:testDebugUnitTest --tests "com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListViewModelTest"`
- `./gradlew :feature:movie-catalog:testDebugUnitTest --tests "com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListViewModelTest.shouldEmitSuccessStateWhenFetchingMovies"`

## Architecture checks
- `./gradlew :test:konsist:test` (architecture convention tests).

## Instrumentation tests
- `./gradlew connectedDebugAndroidTest` (all device tests).
- `./gradlew :app:connectedDebugAndroidTest` (app device tests).

Run a single instrumentation test
- `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.MyTest`
- `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.MyTest#testMethod`

## Architecture rules
- Keep layer boundaries: UI -> domain -> data.
- Repository interfaces live in `core:domain`.
- Repository implementations live in `core:data` or feature data modules.
- Data sources (network/database) are hidden behind repositories.
- Use mappers per layer (`ApiToDomainMapper`, `ApiToDbMapper`, `DomainToUiMapper`).
- Feature modules should not depend on other feature modules.
- Shared code goes to `core` modules only when used by 2+ features.

## MVI and UI state
- Each screen defines a sealed `Event`, sealed `SideEffect`, and `UiState` data class.
- ViewModels extend `MviViewModel` and expose `viewState: StateFlow`.
- Emit navigation/toast as side effects via `setEffect`.
- Use immutable UI state and prefer `ImmutableList` for collections.

## Use cases and flows
- Use cases implement `UseCase<I, M>` and define `operator fun invoke`.
- Use cases return `Flow<ResultState<T>>`.
- Wrap flows with `onStartCatch(coroutineContext, logger)`.
- Use `onLoading`, `onSuccess`, `onError` in ViewModels.

## Error handling
- Represent errors with `ErrorModel` variants.
- Convert network/db errors to `ErrorModel` in data layer.
- Show user-facing errors via side effects, not directly in state mutation.

## Dependency injection
- Koin is the DI framework. Keep modules small and feature-scoped.
- Use `core:bridge-di` to avoid cyclic Gradle dependencies.

## Code style and formatting
- Kotlin style is enforced by Spotless + ktlint.
- Do not use wildcard imports.
- Prefer explicit imports and keep Android/Kotlin/third-party grouped by ktlint.
- Use trailing commas in multiline parameter lists and argument lists.
- Indent with 4 spaces.
- Ktlint overrides: property naming is relaxed; `@Composable` function names are exempt.

## Naming conventions
- Classes and interfaces: `PascalCase` (e.g., `MovieListViewModel`).
- Functions and properties: `lowerCamelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- Use case classes end with `UseCase` and expose `operator fun invoke`.
- Repository interfaces end with `Repository` and implementations end with `Impl`.
- Data sources end with `DataSource`.
- Mappers end with `Mapper` and implement the architecture interfaces.
- `@Composable` functions may use `PascalCase` (ktlint allows this).

## Compose and UI guidelines
- UI is Jetpack Compose only; avoid XML UI changes unless required.
- State is collected from `StateFlow` and rendered as immutable UI state.
- Keep screens thin; move logic to ViewModel/use cases.
- Use `Channel`/`Flow` for one-off effects, not UI state mutation.

## Coroutines and dispatchers
- Prefer injected `CoroutineContext` (Koin) over hardcoded `Dispatchers`.
- Use `CoroutinesDispatchers` named qualifiers for DI lookups.
- Keep flows cold in use cases and convert to hot state in ViewModels.

## Data models and mapping
- Use distinct models per layer (API, DB, domain, UI).
- Map between layers with `Mapper` interfaces from `architecture:mapper`.
- Keep mapping logic out of ViewModels and composables.

## Logging
- Use the `Logger` abstraction instead of direct platform logging.
- Log exceptions via `onStartCatch` in use cases.

## Network and database
- Retrofit is used for network data sources in `core:network:retrofit`.
- Room is used for database data sources in `core:database:room`.
- Access network/database via repository interfaces, not directly from UI.

## Testing conventions
- Use JUnit4 with `@Test` and `runTest` from `kotlinx.coroutines.test`.
- Use Turbine for `Flow` assertions.
- Use Truth for assertions and Mockative for mocking.
- Use `StandardTestDispatcher` and `Dispatchers.setMain` in ViewModel tests.
- Mockative uses KSP (`kspTest`); ensure generated sources are not edited.
- Place shared test doubles in `test/doubles/*` modules.

## Module structure reminders
- `app` integrates features and core modules.
- `feature/*` contains screen UI, ViewModels, feature-specific models.
- `core/*` contains shared domain, data, presentation, network, database.
- `architecture/*` contains shared mapper interfaces.
- `test/*` contains test doubles and konsist checks.

## Gradle and build-logic
- Custom convention plugins live in `build-logic/convention`.
- App and library modules apply the custom plugins (lint, spotless, koin).
- Lint runs with `warningsAsErrors = true` and `abortOnError = true`.
- Spotless formats `*.kt`, `*.kts`, and `*.xml`, excluding build dirs.

## Dependency rules
- Do not add feature-to-feature dependencies.
- Prefer API modules (`core:network:api`, `core:database:api`) over impls.
- Keep DI modules scoped to their feature or layer.

## What to avoid
- Do not bypass lint/format tasks unless explicitly requested.
- Do not add feature-to-feature dependencies.
- Do not commit secrets or `local.properties`.
