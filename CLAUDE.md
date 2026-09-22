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

### Mappers
All mappers implement typed interfaces from `:architecture:mapper` (Konsist-enforced):
- `ApiToDomainMapper<ApiModel, DomainModel>` → `fromApiToDomain()`
- `DbToDomainMapper<DbModel, DomainModel>` → `fromDbToDomain()`
- `ApiToDbMapper<ApiModel, DbModel>` → `fromApiToDb()`
- `DbToEntityMapper<DbModel, Entity>` → `fromDbToEntity()`
- `EntityToDbMapper<Entity, DbModel>` → `fromEntityToDb()`
- `DomainToUiMapper<DomainModel, UiModel>` → `fromDomainToUi()`

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
| Mappers | `Mapper` | must implement a `:architecture:mapper` interface |

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
