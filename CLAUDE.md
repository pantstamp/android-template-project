# CLAUDE.md — Android Template Project

## Build & Run

- **Requires** `TMDB_API_KEY=<token>` in `local.properties` (root). Missing key = empty BuildConfig field, not a build error.
- Build/run via Android Studio or `./gradlew assembleDebug`.
- Run all tests: `./gradlew test`
- Run Konsist architecture tests: `./gradlew :test:konsist:test`
- Run Spotless check: `./gradlew spotlessCheck` — fix: `./gradlew spotlessApply`

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

### Mappers
All mappers implement typed interfaces from `:architecture:mapper`:
- `ApiToDomainMapper<ApiModel, DomainModel>` → `fromApiToDomain()`
- `DbToDomainMapper<DbModel, DomainModel>` → `fromDbToDomain()`
- `ApiToDbMapper<ApiModel, DbModel>` → `fromApiToDb()`
- `DomainToUiMapper<DomainModel, UiModel>` → `fromDomainToUi()`

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

- `ApiModel` classes: must be `data class`, `@Serializable`, all `val` with `@SerialName`
- `DbModel`/`Entity` classes: must be `data class`, all `val`, no functions
- `NetworkDataSource` impl: must be `internal`, all non-override properties `private val`
- `DatabaseDataSource` impl: must be `internal`, all non-override properties `private val`
- No `m` prefix on fields (e.g. `mValue` is forbidden)
- `companion object` must be last declaration in a class

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

- **Kotlin**: 2.2.21, Java 17
- **Coroutines**: 1.10.2
- **Compose BOM**: 2025.10.01
- **Koin**: 4.1.1
- **Retrofit**: 3.0.0 + OkHttp 5.2.1
- **Room**: 2.8.3
- **KSP**: 2.3.0
- **Navigation Compose**: 2.9.5
- **kotlinx.collections.immutable**: 0.4.0 — use `ImmutableList` in `UiState`
- **kotlinx-datetime**: 0.7.0 — `Instant` moved to `kotlin.time.Instant` (not `kotlinx.datetime.Instant`); requires `@file:OptIn(kotlin.time.ExperimentalTime::class)` at call sites.
- All versions are in `gradle/libs.versions.toml`. Never hardcode versions in `build.gradle.kts`.

---

## Product Flavors

Currently **no active flavors**. `FlavorDimension` and `AppFlavor` enums in `build-logic` are placeholders — add flavor entries there if needed.

---

## Base Package

`com.pantelisstampoulis.androidtemplateproject` (from `gradle.properties: configuration.package.project`)
