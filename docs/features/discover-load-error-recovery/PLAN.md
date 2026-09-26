# Implementation Plan: Discover Load Error Recovery

## Overview

Fixes GitHub issue #23: the Discover tab goes blank, with no way to recover, when the first movie load
fails (or succeeds with no movies) and nothing is cached.

The approach, in three phases, each leaving the project compiling and green:

1. **Data** — `MoviesRepositoryImpl.getMovies(ignoreCache = true)` returns the cached movies when the
   network succeeds with an empty list and the cache is non-empty.
2. **ViewModel** — `MovieListViewModel` models full-screen loading, refreshing, a typed error and data as
   separate fields; loads once from `init`; handles a single `Refresh` event for Retry and pull; cancels a
   running load before starting another; reports a failed refresh over cached data with a typed
   `RefreshFailed` side effect. The screen gets only the wiring needed to compile and show the toast.
3. **UI** — error and empty states (icon, message, Retry, pull-to-refresh, TalkBack live region), the real
   refreshing flag on the list's pull indicator, and the two new vector drawables.

No new libraries, no new modules, no schema change, no change to `:core` other than the repository.

## References

- Spec: `docs/features/discover-load-error-recovery/SPEC.md`
- Project conventions: `CLAUDE.md`
- Issue: GitHub #23

Path prefixes used below:

- `FEATURE` = `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog`
- `FEATURE_TEST` = `feature/movie-catalog/src/test/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog`
- `MOVIELIST` = `FEATURE/presentation/screen/movielist`

## Decisions (agreed with the user before this plan was written)

1. **"Empty refresh keeps the cache" lives in the repository**, not the ViewModel. Cache validity is
   the repository's job (`docs/ARCHITECTURE.md`: repositories "determine the caching policy"), and
   `getMovies` already holds the cached rows. The ViewModel stays a plain "show what the use case
   returned" mapper. Consequence: the spec's ViewModel test "refresh returns empty with cached data → data
   kept" becomes a **repository** test (Phase 1); the ViewModel never sees an empty list while data is
   cached.
2. **Flat state fields**, not a sealed screen state — matches `WatchedMovieListUiState`.
3. **Typed error is a feature-local enum** `MovieListError { Offline, Generic }`. The ViewModel maps
   `DomainError` onto it; the composable maps it to a string and an icon. `@StringRes` alone (the Watched
   tab's approach) cannot also select the icon.
4. **Pull-to-refresh on the error/empty states uses Material 3 `PullToRefreshBox` directly in the
   feature.** `PullToRefreshLazyColumn` in `:core:presentation:common-ui` is not generalised — it has one
   caller, and `:core` holds only what 2+ features use.

## Observations on existing code

### Deviations — do NOT follow the existing pattern here

- **Initial load is triggered from `MovieListViewModel.init {}`, not `LifecycleEventEffect(ON_CREATE)`.**
  CLAUDE.md forbids starting loads from lifecycle events; `WatchedMovieListViewModel` already loads from
  `init`. Remove the `LifecycleEventEffect` from `MovieListScreen` entirely. This is also what stops
  rotation from firing a second request (spec, Edge Cases).
- **`MovieListEvent.GetMovies(ignoreCache)` is removed** and replaced by `data object Refresh`. The UI never
  asks for a cached load; the only cached load is the one `init` makes.
- **Do not copy `MovieListViewModel`'s current `onError` block** (sets only `isLoading = false`, builds a
  string from `error.message ?: "An error occurred"`). It is the bug. Never read `DomainError.message` in
  presentation code — `ErrorClassifier` fills it with hardcoded English.
- **Do not copy the current `when` order in `MovieListScreen`** (`isLoading` first). Non-empty data must
  win over the loading flags, or a refresh hides the list.
- **`MovieListViewModelTest.shouldEmitErrorStateWhenFetchingMoviesFails` asserts the bug**
  (`errorMessage` is null after an error, `ShowToast(message)` sent). Delete it; do not "fix it up". It is
  replaced by the tests in Phase 2.
- **Load job is tracked and cancelled.** The existing `viewModelScope.launch { … collect { … } }` inside
  `handleEvents` starts an untracked collection per event. Keep a `loadJob: Job?` and cancel it before
  each new load (CLAUDE.md, Flow and coroutines).

### Notes — spotted nearby, out of scope, for separate triage

- `MviViewModel.setState` calls `_viewState.update { viewState.value.reducer() }`, ignoring the value
  `update` supplies. That defeats `update`'s compare-and-set retry, so concurrent reducers can lose
  writes. Harmless while all state updates run on Main; should be `update { it.reducer() }`.
- `MoviesRepositoryImpl.getMovie(movieId)` returns `Error(DomainError.NotFound())` for a missing row;
  CLAUDE.md says absence is `Success(null)`.
- `MovieDao.insertMovies` uses `REPLACE` and nothing ever deletes, so movies that drop off TMDB's list stay
  in the cache indefinitely and reappear on the next cold start.

---

## Phase 1: Data Layer — empty refresh keeps the cache

### Files to modify

- `core/data/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/data/repository/MoviesRepositoryImpl.kt`

  Pass the cached rows into the network fetch so an empty network result can fall back to them:

  ```kotlin
  override fun getMovies(ignoreCache: Boolean): Flow<ResultState<List<Movie>>> = flow {
      val movies = databaseDataSource.getMovies().firstOrNull().orEmpty()
      if (movies.isNotEmpty() && !ignoreCache) {
          emitMoviesFromDb(movies)
      } else {
          fetchMoviesFromNetwork(cachedMovies = movies)
      }
  }

  private suspend fun FlowCollector<ResultState<List<Movie>>>.fetchMoviesFromNetwork(
      cachedMovies: List<MovieDbModel>,
  ) {
      when (val moviesNetworkResult = networkDataSource.getMovies()) {
          is NetworkResult.Success -> {
              val movieList = moviesNetworkResult.data.map(mappers.movieDataMapper::fromApiToDb)
              if (movieList.isEmpty()) {
                  // An empty response does not invalidate the cache: serve what we already have.
                  // With nothing cached this emits Success(emptyList()), which the UI shows as empty.
                  emitMoviesFromDb(cachedMovies)
              } else {
                  databaseDataSource.insertMovies(movieList)
                  emitMoviesFromDb(movieList)
              }
          }

          is NetworkResult.Error, is NetworkResult.Exception -> { /* unchanged */ }
      }
  }
  ```

  Behaviour is unchanged for every other path (cache hit, non-empty network result, network error). Keep
  the existing comments' density; one comment on the empty branch is enough.

### Tests to add

- `core/data/src/test/kotlin/com/pantelisstampoulis/androidtemplateproject/data/repository/MoviesRepositoryImplTest.kt`
  - `shouldReturnCachedMoviesWhenRefreshReturnsEmptyList` — `databaseDataSource.getMovies()` returns two
    `DatabaseTestDoubleFactory.provideMovieDbModel()`; `networkDataSource.getMovies()` returns
    `NetworkResult.Success(emptyList())`; `getMovies(ignoreCache = true)` emits `Success` whose data equals
    the cached rows mapped with `dataMappers.movieDomainMapper`; `coVerify { databaseDataSource.insertMovies(any()) }.wasNotInvoked()`;
    `awaitComplete()`.
  - `shouldReturnEmptyListWhenNetworkReturnsEmptyAndCacheIsEmpty` — database empty, network
    `Success(emptyList())`, `getMovies(ignoreCache = false)` emits `Success(emptyList())`; `awaitComplete()`.

  Follow the existing test style in the file (`coEvery` — both data-source functions are `suspend`).

### Verification

```bash
./gradlew :core:data:test
```

---

## Phase 2: ViewModel — state, events, side effects, tests

### Files to create

- `MOVIELIST/MovieListError.kt`

  ```kotlin
  package …feature.moviecatalog.presentation.screen.movielist

  enum class MovieListError { Offline, Generic }

  internal fun DomainError.toMovieListError(): MovieListError = when (this) {
      is DomainError.NoNetworkConnection -> MovieListError.Offline
      else -> MovieListError.Generic
  }
  ```

  Do not name the function or a class `*Mapper` — this is a many-to-one classification, not a model
  mapping (same reasoning as `ErrorClassifier` in CLAUDE.md), and Konsist would require a mapper
  interface.

### Files to modify

- `MOVIELIST/MovieListEvent.kt` — replace `GetMovies` with `Refresh`:

  ```kotlin
  sealed interface MovieListEvent : Event {
      data object Refresh : MovieListEvent
      data class ShowMovieDetails(val movieId: Int) : MovieListEvent
  }
  ```

- `MOVIELIST/MovieListSideEffect.kt` — replace `ShowToast(text: String)` with:

  ```kotlin
  data class RefreshFailed(val error: MovieListError) : MovieListSideEffect
  ```

- `MOVIELIST/MovieListViewModel.kt`

  UiState (same file, as today):

  ```kotlin
  data class MovieListUiState(
      val isLoading: Boolean = true,        // full-screen spinner; true initially because init loads
      val isRefreshing: Boolean = false,    // pull indicator over a visible list
      val error: MovieListError? = null,    // set only when there are no movies to show
      val data: ImmutableList<MovieUiModel>? = null,
  ) : UiState {
      val hasMovies: Boolean get() = !data.isNullOrEmpty()
  }
  ```

  `isLoading` defaults to `true` so the first frame shows the spinner rather than blank; the
  `PreviewMovieCatalogTabbedScreen` call `MovieListUiState(isLoading = true)` still compiles.

  ViewModel:

  ```kotlin
  class MovieListViewModel(
      private val getMoviesUseCase: GetMoviesUseCase,
      private val mapper: MovieUiMapper,
  ) : MviViewModel<MovieListEvent, MovieListUiState, MovieListSideEffect>(
      initialState = MovieListUiState(),
  ) {

      private var loadJob: Job? = null   // declare BEFORE init — initialisers run in order

      init {
          loadMovies(ignoreCache = false)
      }

      override fun handleEvents(event: MovieListEvent) {
          when (event) {
              MovieListEvent.Refresh -> loadMovies(ignoreCache = true)
              is MovieListEvent.ShowMovieDetails -> setEffect {
                  MovieListSideEffect.NavigateToMovieDetails(event.movieId)
              }
          }
      }

      private fun loadMovies(ignoreCache: Boolean) {
          loadJob?.cancel()
          loadJob = viewModelScope.launch {
              getMoviesUseCase(input = ignoreCache).collect { resultState ->
                  resultState
                      .onLoading {
                          setState {
                              if (hasMovies) copy(isRefreshing = true)
                              else copy(isLoading = true, error = null)
                          }
                      }
                      .onSuccess { movies ->
                          setState {
                              copy(
                                  isLoading = false,
                                  isRefreshing = false,
                                  error = null,
                                  data = movies.map { mapper.fromDomainToUi(it) }.toImmutableList(),
                              )
                          }
                      }
                      .onError { domainError ->
                          val error = domainError.toMovieListError()
                          val hadMovies = viewState.value.hasMovies
                          setState {
                              copy(
                                  isLoading = false,
                                  isRefreshing = false,
                                  error = if (hadMovies) null else error,
                              )
                          }
                          if (hadMovies) setEffect { MovieListSideEffect.RefreshFailed(error) }
                      }
              }
          }
      }
  }
  ```

  Rules this encodes (check each when reviewing):
  - Loading vs refreshing is decided by **whether movies are showing**, not by `ignoreCache`. Retry from
    the error or empty state (no movies) → full-screen spinner; pull over a list → pull indicator.
  - **Both terminal branches reset both flags** (CLAUDE.md: a flag set before launch is reset on every
    exit path). A cancelled job needs no reset: the job that cancelled it emits `Loading` then a terminal
    state of its own.
  - First-load failure sets `error` and sends **no** side effect (spec AC-1.7).
  - A refresh failure over a list keeps `data`, leaves `error = null`, and sends `RefreshFailed` (AC-2.4).
  - No user-facing string anywhere in the ViewModel.

- `MOVIELIST/MovieListScreen.kt` — minimal wiring so the project compiles and the toast works; the full
  error/empty UI is Phase 3.
  - Delete the `LifecycleEventEffect(ON_CREATE)` block and its imports.
  - Replace the `when` with the Phase 3 order, using a plain `Text` for the error branch for now:

    ```kotlin
    when {
        state.hasMovies -> MovieList(movies = state.data!!, isRefreshing = state.isRefreshing, onEvent = onEvent)
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> Text(text = stringResource(state.error.messageRes()), …)
        state.data != null -> Unit   // empty state arrives in Phase 3
    }
    ```

    Prefer a smart-cast-friendly form over `!!` if spotless/lint complain (e.g. `val movies = state.data`
    captured before the `when`, then `!movies.isNullOrEmpty() -> MovieList(movies = movies, …)`).
  - `MovieList` gains `isRefreshing: Boolean`; delete the local `var isRefreshing by remember { … }` and
    its now-unused imports (`mutableStateOf`, `remember`, `getValue`, `setValue`). `onRefresh` sends
    `MovieListEvent.Refresh`.
  - Side-effect handler: replace the `ShowToast` branch with

    ```kotlin
    is MovieListSideEffect.RefreshFailed ->
        Toast.makeText(context, context.getString(sideEffect.error.messageRes()), Toast.LENGTH_SHORT).show()
    ```

  - Add, at the bottom of the file:

    ```kotlin
    @StringRes
    private fun MovieListError.messageRes(): Int = when (this) {
        MovieListError.Offline -> R.string.movie_list_error_offline
        MovieListError.Generic -> R.string.movie_list_error_generic
    }
    ```

- `feature/movie-catalog/src/main/res/values/strings.xml` — new section:

  ```xml
  <!-- Movie list (Discover) screen -->
  <string name="movie_list_error_offline">You\'re offline. Check your connection and try again.</string>
  <string name="movie_list_error_generic">Couldn\'t load movies. Please try again.</string>
  ```

  Leave `error_generic` alone — the Watched tab still uses it.

### Tests — rewrite `FEATURE_TEST/presentation/screen/movielist/MovieListViewModelTest.kt`

Keep the existing Koin/Mockative setup (`@Mock getMoviesUseCase`, `StandardTestDispatcher` set as Main,
`MovieUiMapper` from Koin, `viewModel by inject()`). Adapt to the `init` load, following
`WatchedMovieListViewModelTest`:

- **Stub before first touching `viewModel`** — the `inject()` is lazy and construction starts the load.
- Stub by argument to tell the initial load from a refresh:
  `every { getMoviesUseCase(false) }` for `init`, `every { getMoviesUseCase(true) }` for `Refresh`. Use
  `every`, not `coEvery` — `invoke` returns a `Flow` and is not `suspend`.
- Assert on `viewModel.viewState.value` after `runCurrent()` / `advanceUntilIdle()`. To observe an
  intermediate loading state, stub a flow that suspends between emissions
  (`flow { emit(ResultState.Loading); delay(1_000); emit(ResultState.Success(movies)) }`) and check state
  after `runCurrent()`, before `advanceUntilIdle()`. Without the `delay`, `StateFlow` conflates `Loading`
  and `Success` and the loading state is unobservable.
- Effects: `viewModel.effect.test { … }`; for "no effect" use `expectNoEvents()`.

Test cases (replace all existing ones except the navigation test):

| Test | Stubs | Assert |
|---|---|---|
| `shouldShowLoadingThenMoviesOnInitialLoad` | `false` → Loading, delay, Success(2 movies) | after `runCurrent`: `isLoading`, `data == null`; after idle: `!isLoading`, `data == mapped`, `error == null`; `verify { getMoviesUseCase(false) }.wasInvoked(exactly = once)` |
| `shouldShowOfflineErrorWhenFirstLoadFailsWithNoNetwork` | `false` → Error(`NoNetworkConnection()`) | `error == Offline`, `!isLoading`, `data == null`; `effect.expectNoEvents()` |
| `shouldShowGenericErrorWhenFirstLoadFailsWithOtherError` | `false` → Error(`ServerError()`) | `error == Generic` |
| `shouldShowEmptyStateWhenFirstLoadReturnsNoMovies` | `false` → Success(emptyList) | `data` is empty, `error == null`, `!isLoading` |
| `shouldShowLoadingThenMoviesWhenRetryingAfterFailure` | `false` → Error; `true` → Loading, delay, Success | after `Refresh` + `runCurrent`: `isLoading`, `error == null`, `!isRefreshing`; after idle: `data == mapped`, `error == null` |
| `shouldShowErrorAgainWhenRetryFails` | `false` → Error(`ServerError`); `true` → Error(`NoNetworkConnection`) | `error == Offline` (most recent type) |
| `shouldRefreshWithoutFullScreenLoadingWhenMoviesShowing` | `false` → Success; `true` → Loading, delay, Success(other movies) | after `runCurrent`: `isRefreshing`, `!isLoading`, `data` still old list; after idle: `!isRefreshing`, `data == new` |
| `shouldKeepMoviesAndSendRefreshFailedWhenRefreshFails` | `false` → Success; `true` → Error(`NoNetworkConnection`) | `data` unchanged, `error == null`, `!isRefreshing`, `!isLoading`; effect `RefreshFailed(Offline)` |
| `shouldApplyOnlyLatestLoadWhenRefreshStartsDuringLoad` | `false` → delay(1_000), Success(listA); `true` → Success(listB) | send `Refresh` before advancing; after idle `data == listB` (listA never applied) |
| `shouldNavigateToMovieDetailsWhenShowMovieDetailsEventIsTriggered` (keep) | `any()` → `emptyFlow()` (init now calls the use case) | unchanged |

`DomainTestDoubleFactory.provideMovieModel()` supplies movies. It randomises every field, so separate
calls give distinct lists. `id` is drawn from 1–999, though, so two lists can share an id. Where a test
compares an old list with a new one, build both and assert on the mapped lists as a whole, not on ids.

### Verification

```bash
./gradlew :feature:movie-catalog:testDebugUnitTest assembleDebug spotlessCheck
```

---

## Phase 3: UI — error and empty states, icons, accessibility

### Files to create

- `feature/movie-catalog/src/main/res/drawable/ic_cloud_off.xml`
- `feature/movie-catalog/src/main/res/drawable/ic_error.xml`

  Android vector drawables from Material Symbols (fonts.google.com/icons, "Outlined", weight 400, 24dp),
  icons `cloud_off` and `error`. Same format as the existing `ic_star.xml` (24dp, 960 viewport,
  `#FF000000` fill — tinted at runtime). No `material-icons-extended` dependency.

### Files to modify

- `feature/movie-catalog/src/main/res/values/strings.xml` — add to the Discover section:

  ```xml
  <string name="movie_list_empty">No movies to show right now.</string>
  <string name="action_retry">Retry</string>
  ```

- `MOVIELIST/MovieListScreen.kt`

  Final `when` (replaces the Phase 2 interim version):

  ```kotlin
  when {
      !movies.isNullOrEmpty() -> MovieList(movies, isRefreshing = state.isRefreshing, onEvent = onEvent)
      state.isLoading -> CircularProgressIndicator()
      state.error != null -> MovieListStatus(
          messageRes = state.error.messageRes(),
          iconRes = state.error.iconRes(),
          onRetry = { onEvent(MovieListEvent.Refresh) },
      )
      movies != null -> MovieListStatus(
          messageRes = R.string.movie_list_empty,
          iconRes = null,
          onRetry = { onEvent(MovieListEvent.Refresh) },
      )
  }
  ```

  where `val movies = state.data` is read before the `when`.

  New private composable in the same file:

  ```kotlin
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  private fun MovieListStatus(
      @StringRes messageRes: Int,
      @DrawableRes iconRes: Int?,
      onRetry: () -> Unit,
      modifier: Modifier = Modifier,
  ) {
      PullToRefreshBox(
          isRefreshing = false,          // a retry swaps this whole state for the full-screen spinner
          onRefresh = onRetry,
          modifier = modifier.fillMaxSize(),
      ) {
          // PullToRefreshBox only reacts to nested scroll, so the content must be scrollable.
          LazyColumn(modifier = Modifier.fillMaxSize()) {
              item {
                  Column(
                      modifier = Modifier
                          .fillParentMaxSize()
                          .padding(horizontal = 16.dp),
                      verticalArrangement = Arrangement.Center,
                      horizontalAlignment = Alignment.CenterHorizontally,
                  ) {
                      if (iconRes != null) {
                          Icon(
                              painter = painterResource(id = iconRes),
                              contentDescription = null,
                              tint = MaterialTheme.colorScheme.onSurfaceVariant,
                              modifier = Modifier.size(48.dp),
                          )
                          Spacer(modifier = Modifier.height(16.dp))
                      }
                      Text(
                          text = stringResource(id = messageRes),
                          style = MaterialTheme.typography.bodyLarge,
                          textAlign = TextAlign.Center,
                          modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                      )
                      Spacer(modifier = Modifier.height(24.dp))
                      Button(onClick = onRetry) {
                          Text(text = stringResource(id = R.string.action_retry))
                      }
                  }
              }
          }
      }
  }
  ```

  And next to `messageRes()`:

  ```kotlin
  @DrawableRes
  private fun MovieListError.iconRes(): Int = when (this) {
      MovieListError.Offline -> R.drawable.ic_cloud_off
      MovieListError.Generic -> R.drawable.ic_error
  }
  ```

  Notes:
  - Material 3 `Button` enforces the 48dp minimum touch target via `minimumInteractiveComponentSize`; no
    extra modifier needed (AC-4.3).
  - The screen's outer `Box` keeps `contentAlignment = Alignment.Center` for the spinner; `MovieList` and
    `MovieListStatus` both fill the box.
  - `PullToRefreshBox` is `@ExperimentalMaterial3Api`; opt in at the function, as
    `PullToRefreshLazyColumn` does.

### Verification

```bash
./gradlew spotlessCheck lint test :test:konsist:test assembleDebug
```

Manual check on an emulator (the parts unit tests cannot see):

1. Clear app data, enable airplane mode, launch → offline icon, offline message, Retry. TalkBack on:
   the message is announced when it appears.
2. Tap Retry → full-screen spinner → offline state again.
3. Pull down on the error state → same as Retry.
4. Disable airplane mode, tap Retry → list appears.
5. Pull to refresh with the list showing → list stays, indicator spins, then stops.
6. Enable airplane mode, pull to refresh → list stays, indicator stops, offline toast.
7. Rotate on each of the error state, the list and the spinner → state is kept, no extra request
   (check Logcat / OkHttp logs).

---

## Implementation Notes

- **Spec vs plan on the empty-refresh test.** SPEC.md's Testing section lists "refresh returns empty with
  cached data → data kept, no side effect" under ViewModel tests. By decision 1 that guarantee lives in the
  repository and is tested there (Phase 1). The ViewModel has no branch for it, so there is nothing to test
  at that level.
- **`Unauthorized` / missing API key** falls into `MovieListError.Generic` by design (spec, Edge Cases).
- **Lint runs with `warningsAsErrors`.** Every new string and drawable must be referenced by the end of
  the phase that adds it — which is why the two error strings arrive in Phase 2 (used by the toast) and the
  empty/retry strings and icons in Phase 3.
- **No `androidTest` sources reference the changed classes** (checked: only
  `core/database/room`'s `RoomDataSourceTest`, which uses `DatabaseDataSource`, untouched).
