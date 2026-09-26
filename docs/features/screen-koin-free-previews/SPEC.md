# Feature Spec: Screen Koin-Free Previews

**Author**: Pantelis Stampoulis  
**Date**: 2026-09-26  
**Status**: Draft  
**Source**: GitHub issue #25 — "Screens resolve their effect dispatcher via getKoin(), which breaks every Compose preview" (bug)

---

## Overview

Every `@Preview` that renders one of the three movie-catalog screens throws
`KoinApplication has not been started`, because each screen resolves its effect dispatcher from Koin
inside the composable. A preview has no `Application`, so Koin is never started. The fix removes that
lookup at its source: `ObserveEffects` owns its dispatcher and lifecycle owner, so screens take only
state, effects and callbacks. A Konsist rule then stops presentation code from reaching into Koin from
Compose again, and the previews that could never have rendered are added so that every screen state can
be seen in Android Studio.

### Root cause (verified against the code)

- `WatchedMovieListScreen.kt:96`, `MovieListScreen.kt:108` and `MovieDetailsScreen.kt:110` each call
  `getKoin().get<CoroutineContext>(named(CoroutinesDispatchers.MainImmediate))` to pass to
  `ObserveEffects`. In Koin 4.2.2 `getKoin()` falls back to the global instance when no Koin context is
  provided in Compose, and in a preview that instance was never started.
- `ObserveEffects` (`core/presentation/mvi/.../MviUiHelpers.kt`) requires the `coroutineContext`, so every
  caller has to obtain one. The only value ever supplied is `Dispatchers.Main.immediate`; no test goes
  through `ObserveEffects`, and `Dispatchers.setMain` already replaces `Main.immediate` in unit tests, so
  the DI indirection buys nothing.
- `PreviewWatchedMovieListLoading` / `Empty` / `WithData` and `PreviewMovieCatalogTabbedScreen` have
  therefore never rendered. PR #24 worked around the problem for `MovieListScreen`'s previews only, with a
  preview-only `KoinApplicationPreview` wrapper (`MovieListPreviewKoin`).

### Found beyond the issue

- Nothing prevents a new screen from reintroducing a Koin lookup; CI compiles previews but never renders
  them, so the breakage is silent.
- Several screen states have no preview at all, contrary to the CLAUDE.md rule that every visual state
  gets one: `MovieDetailsScreen` has none; `MovieListScreen` lacks loading, content and refreshing;
  `WatchedMovieListScreen` lacks error.
- CLAUDE.md's Compose section links to #25 and tells authors to wrap such screens in
  `KoinApplicationPreview`; that guidance becomes wrong once this lands.

---

## User Stories

### US-1: Screens build without a DI container

- **As a** developer, **I want** screens to depend only on their parameters, **so that** previews and
  tests can render them without starting Koin.
- **Acceptance Criteria:**
  - [ ] `ObserveEffects` has no `coroutineContext` parameter; it collects on `Dispatchers.Main.immediate`,
        hard-coded inside the function.
  - [ ] `ObserveEffects`'s `lifecycleOwner` parameter defaults to `LocalLifecycleOwner.current`.
  - [ ] The three screens call `ObserveEffects(effect = effect) { … }` and no longer import `getKoin`,
        `named`, `CoroutinesDispatchers` or `CoroutineContext` for this purpose.
  - [ ] No composable in `:feature:movie-catalog` calls `getKoin()`.
  - [ ] The KDoc of `ObserveEffects` states why it uses `Main.immediate` (effects are handled in the same
        frame they are sent, without re-dispatch) and that tests control it through `Dispatchers.setMain`.

### US-2: Every screen state has a rendering preview

- **As a** developer, **I want** a preview for every state a screen can render, **so that** I can check
  UI changes in Android Studio without running the app.
- **Acceptance Criteria:**
  - [ ] `MovieListPreviewKoin` is removed, and the three `MovieListScreen` previews call the screen
        directly.
  - [ ] New previews exist for:
    - `MovieListScreen`: loading, content, content while refreshing (`isRefreshing = true`)
    - `WatchedMovieListScreen`: error (`errorRes` set)
    - `MovieDetailsScreen`: loading, error, content, content with rating in progress
  - [ ] Every `@Preview` in `:feature:movie-catalog`, existing and new, renders in Android Studio without a
        Koin wrapper — checked by opening each one, since CI does not render previews.
  - [ ] Previews use a fixed `UiState`, `emptyFlow()` for effects and no-op callbacks.

### US-3: The regression cannot return unnoticed

- **As a** maintainer, **I want** CI to reject Compose-side Koin lookups in presentation code, **so
  that** a future screen cannot silently break previews again.
- **Acceptance Criteria:**
  - [ ] A Konsist test in `:test:konsist` fails if any file in a `..feature..presentation..` package
        imports from `org.koin.compose..` or `org.koin.androidx.compose..` (covers `getKoin`, `koinInject`,
        `koinViewModel`, `KoinApplicationPreview`).
  - [ ] The test passes on this branch. Koin DSL imports in `presentation/di` (`org.koin.dsl`,
        `org.koin.core`, `org.koin.androidx.viewmodel.dsl`) are unaffected, and `koinViewModel()` in
        `navigation/MovieCatalogNavigation.kt` stays allowed because it is outside `presentation`.
  - [ ] The CLAUDE.md Compose rule referencing #25 is rewritten to state the constraint (screens take
        state, effects and callbacks; no Koin lookups in presentation composables) and to name the
        Konsist test that enforces it.

### US-4: Effects keep working in the app

- **As a** user, **I want** navigation, toasts and snackbars to behave exactly as before.
- **Acceptance Criteria** (manual, on an emulator):
  - [ ] Discover: tapping a movie opens its details (`NavigateToMovieDetails`).
  - [ ] Discover: with movies shown, going offline and pulling to refresh shows the toast
        (`RefreshFailed`).
  - [ ] Watched: tapping a movie opens its details (`NavigateToMovieDetails`).
  - [ ] Details: rating a movie shows the "saved" snackbar (`RatingSaved`).
  - [ ] Details: rotating after a rating does not show the snackbar again.
  - [ ] Details: `RatingError` needs a database failure and cannot be triggered by hand; it is accepted
        by code inspection, since it goes through the same `ObserveEffects` call as `RatingSaved`.

---

## Edge Cases

- **Tabbed screen**: `PreviewMovieCatalogTabbedScreen` composes both list screens through the pager. It
  must render without change once the list screens stop calling `getKoin()`.
- **`MovieDetailsScreen`'s `LifecycleEventEffect(ON_CREATE)`** sends `Init` through `onEvent`; in a
  preview that is a no-op lambda, so it needs no special handling.
- **`Dispatchers.Main` in layoutlib**: already proven to work, since `MovieListPreviewKoin` binds
  `Dispatchers.Main.immediate` and those previews render.
- **Lifecycle**: collection still runs under `repeatOnLifecycle(STARTED)` on the same lifecycle owner as
  before, so behaviour on rotation, backgrounding and back navigation is unchanged.

## Error Handling

No runtime error paths change. The only error removed is the preview-time
`KoinApplication has not been started`.

---

## Out of Scope

- **Removing dispatcher bindings.** `CoroutinesDispatchers.MainImmediate`, `Main` and `Default` stay in
  the enum and in both `dispatcherModule`s even though no code resolves them after this fix;
  `dispatcherModule` is a general catalogue for injected, non-UI classes.
- **`:app`'s `AppNavigation.kt`** keeps its `getKoin()` / `koinInject()` calls; it is nav-host setup,
  not a feature screen, and the Konsist rule does not cover it.
- **Modelling one-off events as state.** Google's architecture guidance discourages `Channel`-based
  effects; moving away from them would redesign the MVI effect mechanism.
- **`MovieDetailsUiState.errorMessage: String`** holds user-facing text in the ViewModel, contrary to
  CLAUDE.md. The error preview uses a literal string; fixing the state type is a separate change.
- **Automated tests for `ObserveEffects`** or rendering previews in CI (Robolectric, Paparazzi,
  Roborazzi). Verification is manual (US-2, US-4); the Konsist rule in US-3 is the only new automated
  check.

## Open Questions

None.
