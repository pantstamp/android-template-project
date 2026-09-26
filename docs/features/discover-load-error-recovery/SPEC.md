# Feature Spec: Discover Load Error Recovery

**Author**: Pantelis Stampoulis  
**Date**: 2026-09-26  
**Status**: Draft  
**Source**: GitHub issue #23 — "Discover tab shows a blank screen with no way to recover when the first movie load fails" (bug)

---

## Overview

When the Discover tab has no cached movies and the network fetch fails, the screen goes blank: a toast
flashes, then nothing is rendered and there is no way to retry short of rotating the device or restarting
the app. The same blank screen appears when the fetch succeeds but returns no movies. This feature gives the
Discover tab explicit **error** and **empty** states with a Retry action and pull-to-refresh, keeps the list
visible while a refresh runs, keeps cached movies when a refresh fails or comes back empty, and moves
user-facing error text out of the ViewModel so it can be localised.

### Root cause (verified against the code)

- `MovieListViewModel` handles `onError` by setting only `isLoading = false` and sending a `ShowToast`
  effect. `errorMessage` is never set, so the state ends as `isLoading = false`, `errorMessage = null`,
  `data = null`, and no branch in `MovieListScreen` renders anything.
- Pull-to-refresh lives in `MovieList` (`PullToRefreshLazyColumn`), which only renders when `data != null`.
- The initial load is triggered by `LifecycleEventEffect(ON_CREATE)`. The pager keeps both tabs composed
  (`beyondViewportPageCount = 1`), so switching tabs never retries. Rotation "fixes" the bug only because
  `ON_CREATE` fires again.
- `MovieListScreen` checks `isLoading` before `data`, so a pull-to-refresh with cached movies replaces the
  whole list with a full-screen spinner.
- The pull-to-refresh indicator reads a local `isRefreshing` that is never set to `true`.
- Every `GetMovies` event launches a new collection without cancelling the previous one, so overlapping
  loads race to set state.
- The toast text falls back to a hardcoded `"An error occurred"` in the ViewModel, and `ErrorClassifier`
  puts hardcoded English strings (e.g. `"No internet connection"`) into `DomainError.message`.

---

## User Stories

### US-1: Recover from a failed first load

**As a** user opening the app with no cached movies and no working connection,  
**I want to** see why the movies didn't load and be able to try again,  
**so that** I am not left looking at a blank screen.

#### Acceptance Criteria

- AC-1.1: If the load fails and no movies are cached, the Discover tab shows an error state: an icon, a
  message and a **Retry** button, centred in the tab.
- AC-1.2: For `DomainError.NoNetworkConnection` the message is "You're offline. Check your connection and
  try again." and the icon is a cloud-off icon.
- AC-1.3: For every other `DomainError` type (timeout, server error, unauthorized, unknown, …) the message is
  "Couldn't load movies. Please try again." and the icon is a generic warning icon.
- AC-1.4: Tapping **Retry** replaces the error state with the full-screen spinner and fetches from the
  network again.
- AC-1.5: If the retry succeeds, the movie list is shown. If it fails, the error state is shown again with
  the message for the new error type.
- AC-1.6: Pulling down on the error state triggers the same retry as the button.
- AC-1.7: No toast is shown when the error state is displayed — the error state is the feedback.

---

### US-2: Refresh without losing what I can already see

**As a** user browsing cached movies,  
**I want** a refresh to keep the list on screen and tell me if it failed,  
**so that** a bad connection never takes away movies I could already see.

#### Acceptance Criteria

- AC-2.1: Pull-to-refresh with movies showing keeps the list visible. No full-screen spinner appears.
- AC-2.2: The pull-to-refresh indicator spins for exactly as long as the refresh is running (it reflects
  real loading state, not a local flag).
- AC-2.3: If the refresh succeeds with movies, the list is updated.
- AC-2.4: If the refresh fails, the indicator stops, the list stays unchanged, and a toast shows the same
  message as the error state would for that error type (AC-1.2 / AC-1.3).
- AC-2.5: If the refresh succeeds but returns an empty list, the indicator stops and the cached list stays
  unchanged. No toast or other message is shown.

---

### US-3: Understand an empty catalogue

**As a** user whose fetch succeeded but returned no movies,  
**I want to** see that there is nothing to show and be able to try again,  
**so that** an empty response doesn't look like a broken screen.

#### Acceptance Criteria

- AC-3.1: If the fetch succeeds with an empty list and no movies are cached, the Discover tab shows an empty
  state with the message "No movies to show right now." and the same **Retry** button.
- AC-3.2: The empty state is not styled as an error (neutral or no icon).
- AC-3.3: Retry and pull-to-refresh on the empty state behave as in AC-1.4 – AC-1.6.

---

### US-4: A screen reader user learns that loading failed

**As a** TalkBack user,  
**I want** the error or empty message to be announced when it appears,  
**so that** I don't depend on a short toast I may miss.

#### Acceptance Criteria

- AC-4.1: The error-state and empty-state messages are in a polite live region, so TalkBack announces
  them when they appear.
- AC-4.2: The icon is decorative (`contentDescription = null`).
- AC-4.3: The Retry button is labelled "Retry" and has a touch target of at least 48dp.

---

## Behaviour Summary

| Situation | What the user sees |
|---|---|
| First load, nothing cached | Full-screen spinner |
| First load fails, nothing cached | Error state (icon, message, Retry), pull-to-refresh enabled |
| First load returns empty, nothing cached | Empty state (message, Retry), pull-to-refresh enabled |
| Retry / pull from error or empty state | Full-screen spinner → list, error state or empty state |
| Pull-to-refresh with movies showing | List stays visible, pull indicator spins |
| Refresh fails with movies showing | Indicator stops, list unchanged, toast with typed message |
| Refresh returns empty with movies showing | Indicator stops, list unchanged, no message |

---

## Edge Cases

- **Overlapping loads** (Retry tapped twice, pull during a running load): a new load cancels the running
  one; only one fetch updates state.
- **Screen rotation**: the initial load runs once per ViewModel (triggered from the ViewModel, not from a
  lifecycle event). Rotation keeps the current state — error, empty, list or loading — and does not fire a
  new request.
- **Process death**: the ViewModel is recreated and runs the initial load again. Cached movies are served
  from the database; with no cache, the normal first-load flow applies.
- **Tab switch** (Watched → Discover): no automatic retry. The current state is kept.
- **Error type changes between attempts** (e.g. offline, then server error): the error state always shows
  the message and icon for the most recent failure.
- **`DomainError.Unauthorized`** (usually a missing or invalid `TMDB_API_KEY`): shown as the generic error;
  Retry is still offered even though it will not succeed until the key is fixed.

---

## Error Handling

- Error text is resolved in the composable from the typed error (a `DomainError` category or `@StringRes`
  id carried in state / side effect). The ViewModel never holds user-facing strings; the
  `"An error occurred"` fallback is removed.
- `DomainError.message` (hardcoded English from `ErrorClassifier`) is never displayed.
- The toast for a failed refresh becomes a typed side effect rather than `ShowToast(text: String)`.
- A failed or empty refresh never clears cached movies from the screen. Whether the repository or the
  ViewModel enforces "empty refresh keeps the cached list" is left to the plan; today
  `MoviesRepositoryImpl.fetchMoviesFromNetwork` emits the network result as-is.

---

## Testing

`MovieListViewModel` unit tests cover at least:

- First load fails, nothing cached → error state with the error type.
- First load fails with `NoNetworkConnection` vs another type → the two distinct error categories.
- Retry after failure → loading, then success with data.
- Refresh fails with cached data → data kept, refreshing cleared, typed error side effect sent.
- First load returns empty, nothing cached → empty state.
- Refresh returns empty with cached data → data kept, no side effect.
- A second load while one is running → only the latest load's result reaches state.

---

## Implementation Notes (constraints, not design)

- The project has no Material icons dependency. The cloud-off and warning icons are added as vector
  drawables in `:feature:movie-catalog` (like `ic_star.xml`), not via `material-icons-extended`.
- Strings go in the feature module's string resources.

---

## Out of Scope

- Automatic retry when connectivity is restored (needs a connectivity observer — separate feature).
- Automatic retry on tab switch or on screen rotation.
- Distinct messages per error type beyond offline vs generic (timeout, server, unauthorized, …).
- A dedicated message or fix flow for a missing/invalid `TMDB_API_KEY`.
- Replacing the refresh-failure toast with a Snackbar.
- Any feedback for an empty refresh that keeps the cached list.
- Localising or restructuring `ErrorClassifier`'s `DomainError.message` strings (they simply stop being shown).
- Error handling on the Watched tab or the movie details screen.

---

## Open Questions

None — all topics were resolved in the interview.
