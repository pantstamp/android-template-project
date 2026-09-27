# Feature Spec: Movie Search

**Author**: Pantelis Stampoulis  
**Date**: 2026-09-27  
**Status**: Draft  
**Source**: GitHub issue #29 — "Search movies by title from the Discover tab" (enhancement)

---

## Overview

The Discover tab shows only the first page of popular movies from TMDB (about 20 titles). There is no way
to reach any other movie, open its details or rate it, which also limits what can end up in the Watched
tab. This feature adds a search field to the Discover tab. It searches TMDB by title as the user types,
and the results replace the Discover list. Results open in Movie Details and can be rated like any other
movie. Clearing the search brings the Discover list back immediately. Searching never adds anything to the
local database: Movie Details reads a movie from the database if it is there and otherwise fetches it from
the network without saving it.

### Current behaviour (verified against the code)

- `MoviesRepositoryImpl.getMovie()` reads only the local `movies` table and emits
  `Error(DomainError.NotFound())` when the row is missing. Movie Details therefore cannot open a movie that
  was never in the Discover list (AC8).
- The `movies` table **is** the Discover list: `getMovies()` returns every row (`SELECT * FROM movies`).
  Any searched movie written to that table would appear in Discover, so search results must never be
  written there (F29, AC10).
- `MovieRow` renders the poster with `AsyncImage` and no placeholder, so a movie without a poster shows an
  empty area. Discover has no placeholder today, contrary to the issue's F19.
- `MovieRow` shows a genre (first entry of `genre_ids`) as well as poster, title, public rating and year.
- Movie Details shows `DomainError.message` as raw text with no Retry action.
- The Discover and Watched tabs share one navigation entry (`MovieListDestination`), so their ViewModels
  survive rotation and tab switches. Process death is not handled by any screen today.
- `WatchedMovieDao.insertWatchedMovie` uses `OnConflictStrategy.ABORT`, so rating an already-rated movie
  fails. This predates search and is out of scope (see below).

---

## Decisions

| Topic | Decision |
|---|---|
| Details for a movie not in Discover | Read the local `movies` table first. If the movie is not there, fetch it from TMDB (`GET movie/{id}`). The fetched movie is **not** saved to the `movies` table. |
| Details errors | Details shows the same offline and generic messages, icons and Retry action as Discover, for every movie (searched or not). No raw error text. |
| Row design | Search results reuse `MovieRow` unchanged, genre included. |
| Poster placeholder | One placeholder is added to `MovieRow`, used for a missing poster and for a failed image load, in both Discover and search. |
| Errors with results on screen | A **new query** that fails replaces the results with the full-screen error. A **refresh of the same query** that fails keeps the results and shows the Discover-style Toast. |
| Process death | The query is restored and the search runs again. Results are not saved. |
| Minimum query length | 2 characters after trimming (resolves issue open question 1). |
| Results shown | First page only, up to 20 (resolves issue open question 2). |
| Scroll position | Discover and search results each keep their own position. A new query starts at the top. |
| Back navigation | With a query in the field (keyboard closed), back clears the search and shows Discover. |

---

## User Stories

### US-1: Search while typing

**As a** user on the Discover tab, **I want to** type a movie title and see matching results while I type,
**so that** I can find a specific movie without scrolling.

**Acceptance Criteria:**
- [ ] **AC1** — Given I am on Discover, when I type "ma", then after a pause of about 300 ms I see search
  results for "ma".
- [ ] **AC2** — Given I am on Discover, when I type "m" (or " m "), then the Discover list stays and no
  search runs.
- [ ] **AC3** — Given I type "matrix" with less than 300 ms between keystrokes, then exactly one search runs,
  for "matrix".
- [ ] **AC4** — Given a search for "mat" is slow and I have already typed "matrix", then only "matrix"
  results are shown, even if the "mat" results arrive later.
- [ ] **AC5** — Given "matrix" is in the field, when I change it to " matrix " (only surrounding spaces
  differ), then no new search runs.
- [ ] **AC6** — Given I search for "zzzzqqq", then I see *No movies found for "zzzzqqq"*.
- [ ] **AC7** — Given results for a query are shown, when I type a different query, then the previous
  results stay visible with a small loading indicator until the new results arrive; the list never goes
  blank between keystrokes.
- [ ] **AC8** — Given new results arrive for a changed query, then the results list is scrolled to the top.
- [ ] **AC9** — Search requests never include adult content (`include_adult=false`).

### US-2: Open and rate a searched movie

**As a** user who found a movie through search, **I want to** open its details and rate it, **so that** it
appears in my Watched tab like any other movie.

**Acceptance Criteria:**
- [ ] **AC10** — Given I tap a search result that was never in the Discover list, then Movie Details shows
  it completely (poster, title, overview, genre, release year, public rating), with no "not found" error.
- [ ] **AC11** — Given I tap a search result that **is** in the local `movies` table, then Movie Details
  loads it from the database and makes no network request for it.
- [ ] **AC12** — Given I rate a movie I found through search, then it appears in the Watched tab with the
  correct title, poster and rating.
- [ ] **AC13** — Given I open a searched movie that was not in Discover, then the `movies` table is unchanged
  afterwards (no row inserted or replaced).
- [ ] **AC14** — Given I open a result and press back, then I return to the same query, results and scroll
  position.
- [ ] **AC15** — Given Movie Details fails to load a movie because I am offline, then it shows the offline
  message, icon and a Retry action. Tapping Retry once I am online loads the movie.
- [ ] **AC16** — Given Movie Details fails to load for any other reason (including a 404 from TMDB), then it
  shows the generic error message, icon and a Retry action.

### US-3: Leave the search

**As a** user who is done searching, **I want to** clear the search and get the Discover list back
immediately, **so that** I can continue browsing.

**Acceptance Criteria:**
- [ ] **AC17** — Given search results are shown, when I tap ✕, then the field is empty and the Discover list
  is shown immediately, without a network request.
- [ ] **AC18** — Given search results are shown, when I delete characters until fewer than 2 remain, then
  the Discover list is shown immediately.
- [ ] **AC19** — Given search results are shown and the keyboard is closed, when I press back, then the
  search is cleared and the Discover list is shown; the app does not close. With an empty field, back
  behaves as it does today.
- [ ] **AC20** — Given I scrolled Discover, searched, and then cleared the search, then Discover is at the
  scroll position I left it at.
- [ ] **AC21** — Given I have searched, when I clear the search and also after I restart the app, then the
  Discover list contains no movie that came only from search.

### US-4: Recover from search errors

**As a** user whose search failed, **I want to** see why and retry, **so that** a network problem does not
leave me stuck.

**Acceptance Criteria:**
- [ ] **AC22** — Given I am offline, when I type a query, then I see Discover's offline message and icon with
  a Retry action. When I go back online and tap Retry, the results for the current query appear.
- [ ] **AC23** — Given any other search failure, then I see Discover's generic error message and icon with a
  Retry action; Retry runs the current query again.
- [ ] **AC24** — Given results for "mat" are shown, when the search for "matrix" fails, then the "mat"
  results are replaced by the full-screen error for "matrix".
- [ ] **AC25** — Given search results are shown, when I pull to refresh, then the current query runs again.
  If it fails, the results stay on screen and the Discover refresh-failed Toast is shown.
- [ ] **AC26** — Given the Discover list is shown, when I pull to refresh, then Discover refreshes as today.

### US-5: Keep the search

**As a** user in the middle of a search, **I want** my query and results to survive everyday interruptions,
**so that** I do not have to search again.

**Acceptance Criteria:**
- [ ] **AC27** — Given I have a query and results, when I rotate the device, then the same query and results
  are shown and no new search runs.
- [ ] **AC28** — Given I have a query and results, when I switch to Watched and back, then the same query and
  results are shown and no new search runs. Switching tabs hides the keyboard.
- [ ] **AC29** — Given I have a query and the system kills the app in the background, when I return to it,
  then the field shows the same query and the search runs again (centered loading indicator, then results
  or an error).
- [ ] **AC30** — Given I close the app (swipe it away) or launch it fresh, then Discover opens with an empty
  search field.

---

## UI Requirements

### Search field
- Shown at the top of the Discover tab, above the list; it does not scroll with the list. The Watched tab
  has no search field.
- Placeholder / label **"Search movies"**, read by TalkBack.
- A clear (✕) button is shown only when the field contains text. Its content description is
  **"Clear search"** and its touch target is at least 48 dp.
- Not focused on launch; the keyboard does not open automatically.
- The keyboard action is Search. Pressing it hides the keyboard and does not start an extra search.
- Scrolling the results or the Discover list hides the keyboard.
- The query sent to TMDB is the trimmed text. Case sensitivity is left to TMDB, which is case-insensitive.

### States
| State | What the user sees |
|---|---|
| Query shorter than 2 characters | The Discover list, in whatever state it is in (list, loading, empty or error) |
| Searching, nothing shown yet | A centered loading indicator |
| Searching, previous results on screen | The previous results with a small loading indicator |
| Results found | The results in TMDB order (relevance), up to 20, each as a `MovieRow` |
| No results | *No movies found for "{query}"*, including the trimmed query |
| Offline | Discover's offline message and icon, with Retry |
| Other error | Discover's generic error message and icon, with Retry |

The no-results, offline and generic messages are polite live regions, as Discover's already are. Result
lists are not announced on each keystroke.

### Poster placeholder
- `MovieRow` shows a neutral film icon on a surface-variant background, at the poster's size, when a movie
  has no poster or its image fails to load. This applies to Discover and search alike.

### Movie Details
- Shows offline and generic error states with the same messages, icons and Retry action as Discover,
  replacing the raw error text. This applies to every movie, not only searched ones.

### Previews
Each state below has a `@Preview`, and each has been opened in Android Studio:
- Search: loading (nothing shown), loading over previous results, results, no results, offline, generic
  error.
- `MovieRow` with the poster placeholder.
- Movie Details: offline error, generic error.

---

## Edge Cases

| Case | Handling |
|---|---|
| Query is only spaces, or 1 character plus spaces | Trimmed length < 2, so Discover is shown and no search runs. |
| Discover's own load failed or is still loading when the user searches | Search works independently. Clearing the search shows Discover in whatever state it is in. |
| User taps a result while a new search is pending (debounce not elapsed) | Opens the tapped movie. Coming back shows whatever the current query produced. |
| Search result is also in the Discover table | Details reads it from the database (AC11). |
| Movie no longer exists on TMDB (404 on `movie/{id}`) | Details shows the generic error with Retry. |
| TMDB returns the same movie twice in a page | Duplicates are dropped by id, keeping the first, so list keys stay unique. |
| Movie has no release date, no overview or no genres | Shown as Discover shows them today (empty year, unknown genre). |
| Very long query | Sent as typed (trimmed). No client-side limit. |
| Rotation while a search is in flight | The in-flight search continues; no second search starts. |
| Rating a searched movie that has already been rated | Behaves as it does for a Discover movie today (see Out of Scope). |

---

## Error Handling

- **Search, offline** (`DomainError.NoNetworkConnection`): the offline state with Retry. If a refresh of the
  same query fails, the results stay and a Toast is shown instead.
- **Search, any other failure**: the generic state with Retry. Same rule for a refresh of the same query.
- **Search, empty result**: this is not an error. It shows the no-results state (`Success(emptyList())`).
- **Details, movie missing locally and fetch fails**: the offline or generic state with Retry, per the error
  type. User-facing text is resolved in the composable (typed error, `@StringRes`), never in the ViewModel.
- **Details, database read fails**: the generic state with Retry.
- **Stale responses**: a search response for anything other than the current query is discarded (AC4).

---

## Out of Scope

- Loading more results on scroll (pagination), for search and Discover
- Recent searches or search history
- Filters (genre, year, rating) and sorting
- Searching the Watched tab
- Offline search among movies saved on the device
- Voice search
- Caching search results or fetched movie details, in memory or on disk
- Re-rating an already-rated movie (`WatchedMovieDao` insert uses `ABORT`); this predates search
- Movie Details re-running its load on rotation (issue #27)
- Offline Movie Details for a watched movie that is not in the Discover table: it shows the offline state.
  A fallback to `watched_movies` would give only partial data (no genre); decided with the Discover-cache
  change in #30.
