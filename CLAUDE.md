# CLAUDE.md — Android Template Project

## Build & Run

- **Requires JDK 21** to run Gradle. The compile toolchain (`java-version` in the catalog) is
  provisioned by the foojay resolver in `settings.gradle.kts`, so no specific JDK install is needed.
- **Requires** `TMDB_API_KEY=<token>` in `local.properties` (root). Missing key = empty BuildConfig field, not a build error.
- Build/run via Android Studio or `./gradlew assembleDebug`.
- Run all tests: `./gradlew test`
- Run Konsist architecture tests: `./gradlew :test:konsist:test`
- **Instrumented tests (`src/androidTest`) are not compiled by CI.** Neither `test` nor
  `assembleDebug` touches them, so they can sit broken indefinitely —
  `core/database/room`'s have not compiled since PR #14. If you change a constructor
  an `androidTest` source uses, check it yourself:
  `./gradlew :core:database:room:compileDebugAndroidTestKotlin`
- Run Spotless check: `./gradlew spotlessCheck` — fix: `./gradlew spotlessApply`
- Run Android Lint: `./gradlew lint` (`abortOnError = true`, `warningsAsErrors = true`)
- CI (`.github/workflows/build.yml`) runs all of the above on every PR.

---

## Module Structure

All modules are declared in `settings.gradle.kts`. Key groupings:

| Path | Role |
|---|---|
| `:app` | Entry point; depends on all feature + core modules |
| `:feature:movie-catalog` | Feature module (UI + ViewModel only) |
| `:core:data` | Repository implementations (single source of truth) |
| `:core:domain` | Use case interfaces/impls + repository interfaces |
| `:core:model` | Domain/business models only |
| `:core:network:api/retrofit/noop` | Network abstraction + Retrofit impl |
| `:core:database:api/room/noop` | Database abstraction + Room impl |
| `:core:presentation:mvi/theme/common-ui/viewmodel` | Shared UI infra |
| `:core:navigation:api/navigation-compose` | Navigation abstraction |
| `:core:bridge-di` | Prevents cyclic Gradle DI dependencies |
| `:architecture:mapper` | Mapper interfaces only |
| `:utils:koin`, `:utils:random` | Shared utilities |
| `:test:konsist` | Architecture rule tests (Konsist) |
| `:test:doubles:*` | Test doubles for DB, network, models |

**Rule**: Feature modules depend only on core modules, never on each other.  
**Rule**: Core modules never depend on feature or app modules.  
**Rule**: Only put classes in `:core` if used by 2+ feature modules.

---

## Convention Plugins (build-logic)

Every module uses one of these in its `build.gradle.kts` — never configure Android/Kotlin directly:

| Plugin ID | Use for |
|---|---|
| `com.pantelisstampoulis.library.core` | Any Android library module |
| `com.pantelisstampoulis.library.feature` | Feature modules (adds Compose, Serialization, domain/presentation deps) |
| `com.pantelisstampoulis.application.core` | `:app` module |
| `com.pantelisstampoulis.application.compose` | `:app` Compose setup |
| `com.pantelisstampoulis.koin` | Adds Koin BOM + common deps |
| `com.pantelisstampoulis.room` | Adds Room + KSP |
| `com.pantelisstampoulis.testing` | Adds test doubles, mockative, turbine, truth |
| `com.pantelisstampoulis.konsist` | Konsist architecture tests |
| `com.pantelisstampoulis.compose` | Compose compiler + dependencies |
| `com.pantelisstampoulis.kotlin.serialization` | KotlinX Serialization |

Use `namespaceWithProjectPackage(suffix = "foo.bar")` in `android { namespace }` — do not hardcode the full package.

SDK/Java version config comes from `gradle.properties` (`configuration.android.*`). Do not hardcode these values.

---

## Architecture: Clean Architecture + MVI

### Layer dependencies (enforced by Konsist)
```
network ──┐
database ──┤──► data ──► domain ──► model
           │              │
           └──────────────┤
                          ▼
                  feature (UI + ViewModel)
                       │
                  presentation
```

### MVI pattern
- ViewModels extend `MviViewModel<Event, UiState, SideEffect>` from `:core:presentation:mvi`
- `Event` = user intent (sealed interface, one per screen)
- `UiState` = immutable data class implementing `UiState` interface
- `UiState` defaults describe a neutral state (not loading, no error, no data). If the
  ViewModel starts in a particular state — e.g. loading, because `init {}` starts a fetch —
  pass it as `initialState = FooUiState(isLoading = true)`. A default that encodes ViewModel
  behaviour makes every other construction (`FooUiState(data = …)` in a preview or test)
  render something else.
- `SideEffect` = one-time effects (navigation, toasts) via `Channel`
- ViewModels call `setState { copy(...) }`, `setEffect { ... }`, never expose mutable state
- UI collects `viewState` as `StateFlow`, `effect` as `Flow`

### Use Cases
- Interface + `internal` impl pattern: `interface FooUseCase`, `internal class FooUseCaseImpl`
- Must reside in `..domain..usecase..` package
- Single public method: `override operator fun invoke(input: T): Flow<ResultState<R>>`
- Impl class uses `onStartCatch()` extension for coroutine error handling
- ViewModels **must not** receive repositories directly — use cases only (Konsist-enforced)

### Repository
- Interface in `:core:domain` at `..domain..repository` package
- Implementation (`*RepositoryImpl`) in `:core:data` at `..data..repository` package
- Every `RepositoryImpl` must have a corresponding `RepositoryImplTest` (Konsist-enforced)
- Returns `Flow<ResultState<T>>` — never suspends directly

### ResultState
```kotlin
// onLoading { }, onSuccess { data -> }, onError { error -> }
```

The error payload is `DomainError` (`:core:model`, `..model.error`). Not named `Error`:
`ResultState` already nests a class of that name and `kotlin.Error` is default-imported.

**Absence is not an error.** "No row found" is a normal outcome — model it as
`Success(null)` with a nullable type, not `Error(DomainError.NotFound())`. Reserving the
error channel for real failures keeps it meaningful when one occurs, and spares every
caller an `onError` branch that silently swallows the non-error case.

### Flow and coroutines

Every rule here comes from a review finding that compiled, passed all five CI checks, and
failed only under cancellation, a race, or repeated navigation. These are the mistakes the
toolchain does not catch.

- **Compose derived flows with `emitAll`**, not `flow { inner.collect { emit(it) } }`.
  The nested form does not propagate upstream cancellation reliably.
- **Never `.collect` a second flow inside the collector of a first.** Chain with
  `flatMapLatest`, or extract a private helper that launches the second collection
  separately. Nested collection blocks the outer stream and is hard to test.
- **A Room-backed `Flow` never completes.** Collecting one from a lifecycle event
  (`ON_RESUME`, `LifecycleEventEffect`) leaks a collector per event — after *N* tab
  switches, *N* collectors race to set state. Subscribe once in `init {}`; if a reload
  must be triggerable, cancel the previous `Job` first.
- **A state flag set before `launch` must be reset on every exit path**, including early
  returns inside `onSuccess` / `onError`. A missed reset leaves the UI stuck with no
  feedback.
- **Inject time, never read it.** Take a `clock: () -> Long` rather than calling
  `System.currentTimeMillis()` in a repository, or the value cannot be asserted in tests.
- **Keep user-facing strings out of ViewModels.** They have no `Context` and cannot be
  localised. Use typed side effects (`RatingSaved` / `RatingError`) or `@StringRes` ids
  and resolve them in the composable.

### Compose

- **Read resources through Compose, not `LocalContext`.** In composition use
  `stringResource()`; in a callback outside composition (e.g. an `ObserveEffects` handler
  showing a Toast) capture `val resources = LocalResources.current` during composition and
  call `resources.getString()`. `LocalContext.current.getString()` fails lint
  (`LocalContextGetResourceValueCall`) under `warningsAsErrors`.
- **Every visual state a screen can render gets a `@Preview`** — loading, each error
  variant, empty, content. CI compiles previews but never renders them, so open them in
  Android Studio before calling them done. Screens that call `getKoin()` throw in a preview
  until [#25](https://github.com/pantstamp/android-template-project/issues/25) lands; wrap
  them in `KoinApplicationPreview` (see `MovieListPreviewKoin` in `MovieListScreen.kt`).

### Mappers
Every mapper declares what it maps by implementing a mapper interface (Konsist-enforced: a
class named `*Mapper` must implement an interface whose name ends in `Mapper`).

`:architecture:mapper` holds only the boundaries that outlive an implementation choice:
- `ApiToDomainMapper<ApiModel, DomainModel>` → `fromApiToDomain()`
- `DbToDomainMapper<DbModel, DomainModel>` → `fromDbToDomain()`
- `ApiToDbMapper<ApiModel, DbModel>` → `fromApiToDb()`
- `DomainToUiMapper<DomainModel, UiModel>` → `fromDomainToUi()`

Every model named there is a domain or abstraction model. Swap Retrofit or Room and all four
survive untouched — that is the test for whether an interface belongs in this module.

**A mapping involving a library-specific type belongs to the module implementing it, not
here.** `:core:database:room` declares its own `internal DbModelToEntityMapper` /
`EntityToDbModelMapper` for converting between `DbModel` and Room entities. An `Entity` is
Room's concept and exists only in that module — `:core:database:api`, `:core:database:noop`
and `:core:data` never see one. Putting that contract in `:architecture:mapper` would make the
stable part of the design name a type belonging to a swappable one.

`DbModel` and `Entity` are not the same layer. `DbModel` lives in `:core:database:api` and is
database-agnostic; `Entity` is the storage representation. `fromEntityToDbModel()` converts
*away* from storage toward the shared abstraction. `DbModel` is spelled out in those two names
because there it is a type, not a destination.

The `*Mapper` name is a promise that the class maps one model to another and declares
that in its type. If a class does something else, give it a different name rather than
an exemption — `ErrorClassifier` turns a `NetworkResult` envelope into a `DomainError`
category, which is a partial, many-to-one classification rather than a model mapping,
so it is not called a mapper.

---

## Naming Conventions (Konsist-enforced)

| Class type | Suffix | Package |
|---|---|---|
| Use case interface | `UseCase` | `..domain..usecase..` |
| Use case impl | `UseCaseImpl` | `..domain..usecase..` |
| Repository interface | `Repository` | `..domain..repository` |
| Repository impl | `RepositoryImpl` | `..data..repository` |
| Network models | `ApiModel` | `..network.model` |
| Database models | `DbModel` or `Entity` | `..database.model` |
| Mappers | `Mapper` | must implement an interface whose name ends in `Mapper` |

- `ApiModel` classes: must be `data class`, `@Serializable`, all `val` with `@SerialName`
- `DbModel`/`Entity` classes: must be `data class`, all `val`, no functions
- `NetworkDataSource` impl: must be `internal`, all non-override properties `private val`
- `DatabaseDataSource` impl: must be `internal`, all non-override properties `private val`
- No `m` prefix on fields (e.g. `mValue` is forbidden)
- `companion object` must be last declaration in a class
- **`@Dao` functions must be `suspend` or return `Flow`** — a function that is neither is a
  blocking query, and Room throws if it reaches the main thread. Konsist-enforced.

---

## Navigation

- Destinations are `@Serializable` objects/data classes in a `sealed interface` per feature
- Feature exposes a `NavGraphBuilder` extension function (e.g. `movieCatalogGraph(...)`)
- ViewModels are obtained via `koinViewModel<T>()` inside composable lambdas
- Use `collectAsStateWithLifecycle()` for state, `toRoute<T>()` for route args
- Navigation is abstracted: `:core:navigation:api` + `:core:navigation:navigation-compose`

---

## Dependency Injection (Koin)

- DI modules are `val fooModule: Module = module { ... }` — `factory` for use cases/repos, `single` for data sources
- Impl classes bound to their interface: `} bind FooInterface::class`
- Impl classes are `internal`; Koin wires them via binding
- Use `getWith("tag")` (from `:utils:koin`) for tagged Logger injection
- Use `get(qualifier = named(CoroutinesDispatchers.IO))` for dispatcher injection

---

## Testing

- Test framework: JUnit4 + Google Truth assertions + Turbine (Flow testing) + Mockative (KSP-based mocking)
- Mockative: annotate fields with `@Mock`, generate via KSP (`kspTest`)
- Repository tests use `KoinTest` with real `testDataModule` + mock overrides
- Test file naming: `FooTest` (suffix `Test`, not `Spec`)
- **No Mockito** — use Mockative only
- Test doubles live in `:test:doubles:*`, not in the module under test
- **Mockative `every` vs `coEvery`**: use `every {}` for non-suspend functions (including Flow-returning ones); use `coEvery {}` only for `suspend` functions. Mixing them causes `InvalidExpectationException` at runtime.
- **Logger in use case tests**: do not mock `Logger` — use an inline no-op object instead. Mockative stubs are not set up for `logger.e(...)`, so an unstubbed call inside `onStartCatch`'s `catch` block will propagate as an uncaught exception that Turbine surfaces as a flow error.
- **Adding tests to a module that has none**: add `id(libs.plugins.custom.testing.get().pluginId)` to the module's `build.gradle.kts` first. Without it, test dependencies (Mockative, Turbine, Truth, test doubles) are not on the classpath.
- **Event and SideEffect files**: each screen puts its `*Event` and `*SideEffect` in separate files (not co-located in the ViewModel file). Follow this convention when creating new screens.
- **Test the branches, not just the listed cases.** When a condition treats two different states the same (e.g. `hasMovies` is false for both `null` and an empty list), test both sides. When a new code path can be reached from more than one caller (e.g. `ignoreCache = true` and `false`), test each one. Assert the same side effects (e.g. "nothing written") across neighbouring tests.

---

## Key Libraries

- **Kotlin**: 2.4.20 — compiles to Java 17; Gradle itself runs on JDK 21
- **Gradle**: 9.5.1 · **AGP**: 8.13.2 · **compileSdk/targetSdk**: 36 · **minSdk**: 24
- **Coroutines**: 1.11.0
- **Compose BOM**: 2026.06.01
- **Koin**: 4.2.2
- **Retrofit**: 3.0.0 + OkHttp 5.4.0
- **Room**: 2.8.5
- **KSP**: 2.3.12
- **Navigation Compose**: 2.9.6
- **kotlinx.collections.immutable**: 0.5.2 — use `ImmutableList` in `UiState`
- **kotlinx-datetime**: 0.8.0 — `Instant` moved to `kotlin.time.Instant` (not `kotlinx.datetime.Instant`); requires `@file:OptIn(kotlin.time.ExperimentalTime::class)` at call sites.
- All versions are in `gradle/libs.versions.toml`. Never hardcode versions in `build.gradle.kts`.

### Pinned on purpose — do not "upgrade" these without reading why

- **AGP stays on 8.x.** AGP 9 needs either built-in Kotlin (which KSP does not support) or the
  deprecated `android.newDsl=false`. Room and Mockative both need KSP. Spike: `chore/agp9-experiment`.
- **Compose, Lifecycle, Navigation, Coil, OkHttp are one minor behind latest.** Their newest
  releases declare `minCompileSdk=37`, which requires AGP 9.1+.
- **Mockative stays on 2.x.** Mockative 3 dropped `@Mock`/`mock()`/`every`/`verify` for runtime
  bytecode mocking; migrating means rewriting the whole test suite.
- **ktlint config**: `max_line_length=120` and forced multi-line class signatures are set in
  `SpotlessUtils.kt`. Without them ktlint 1.8 produces 140-char declarations and collapses
  constructors.

---

## Product Flavors

Currently **no active flavors**. `FlavorDimension` and `AppFlavor` enums in `build-logic` are placeholders — add flavor entries there if needed.

---

## Base Package

`com.pantelisstampoulis.androidtemplateproject` (from `gradle.properties: configuration.package.project`)
