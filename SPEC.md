# Feature Spec: Watched Movies Collection

**Author**: Pantelis Stampoulis  
**Date**: 2026-04-11  
**Status**: Draft  

---

## Overview

Allow users to rate movies (1–10 stars) from the movie details screen. Rated movies are persisted locally and surfaced in a dedicated "Watched" tab on the main movie list screen, showing both the user's personal rating and the public TMDB rating.

---

## User Stories

### US-1: Rate a movie

**As a** user on the movie details screen,  
**I want to** tap a star to give the movie a rating from 1 to 10,  
**so that** my rating is saved and I can find the movie in my Watched collection.

#### Acceptance Criteria

- AC-1.1: The details screen shows a row of 10 star icons when the movie has not been rated yet.
- AC-1.2: Tapping the Nth star selects stars 1–N (gold) and leaves stars N+1–10 unselected (gray).
- AC-1.3: Tapping a star saves the rating and a full snapshot of the movie data to the local database.
- AC-1.4: On successful save, a snackbar appears with the message "Rating saved".
- AC-1.5: After the snackbar appears, the star row becomes non-interactive and a label "You rated this" is displayed above the stars showing the saved rating pre-filled.
- AC-1.6: The user remains on the details screen after rating (no automatic navigation).
- AC-1.7: On save failure, a snackbar appears with the message "Something went wrong. Please try again." The UI reverts to its pre-rating state (stars remain interactive, no rating saved).
- AC-1.8: A rating, once saved, cannot be changed. The rating is immutable.

---

### US-2: View an already-rated movie's details

**As a** user who has previously rated a movie,  
**I want to** see my rating clearly when I open that movie's details screen,  
**so that** I know I have already rated it and cannot accidentally re-rate it.

#### Acceptance Criteria

- AC-2.1: When opening the details screen of a rated movie, the label "You rated this" is shown.
- AC-2.2: Below the label, the star row is pre-filled with the user's rating and is non-interactive.
- AC-2.3: There is no Rate button or interactive rating affordance visible for an already-rated movie.

---

### US-3: Browse rated movies in the Watched tab

**As a** user who has rated one or more movies,  
**I want to** see all my rated movies in a dedicated "Watched" tab,  
**so that** I can revisit movies I have seen and recall my personal ratings.

#### Acceptance Criteria

- AC-3.1: The main movie list screen has two tabs: "Discover" (default) and "Watched".
- AC-3.2: "Discover" is the selected tab when the screen is first opened.
- AC-3.3: The Watched tab lists all movies the user has rated, sorted by most recently rated first.
- AC-3.4: Each list item shows: movie poster, title, public TMDB rating (existing style), and the user's personal rating as a compact icon + number (e.g. ⭐ 8).
- AC-3.5: The user's personal rating compact element has a TalkBack content description: "Your rating: N out of 10".
- AC-3.6: The user cannot interact with or change their rating from the Watched tab (read-only).
- AC-3.7: When the Watched tab is empty, a text-only empty state is shown (e.g. "No movies rated yet. Start exploring and rate movies you've watched.").
- AC-3.8: All data in the Watched tab is loaded from the local database and is available offline.
- AC-3.9: The Watched tab loads all rated movies at once (no pagination).

---

### US-4: Tab and scroll behavior

**As a** user switching between tabs,  
**I want** the app to behave predictably when I navigate between Discover and Watched,  
**so that** I don't lose my place or experience jarring reloads.

#### Acceptance Criteria

- AC-4.1: Returning to the Discover tab restores the previous scroll position.
- AC-4.2: If the Discover tab was loading when the user switched away, the load is cancelled. When the user returns to Discover, the data reloads from scratch.
- AC-4.3: Pull-to-refresh is available only on the Discover tab, consistent with current behavior.
- AC-4.4: The Watched tab does not support pull-to-refresh.

---

## Data Model

### Snapshot stored on rating (local DB)

| Field | Type | Notes |
|---|---|---|
| `movieId` | Int | TMDB movie ID, primary key |
| `title` | String | Movie title at time of rating |
| `posterUrl` | String? | Poster URL at time of rating |
| `overview` | String? | Movie overview at time of rating |
| `publicRating` | Float | TMDB public rating at time of rating |
| `releaseDate` | String? | Release date at time of rating |
| `userRating` | Int | User's rating, 1–10 |
| `ratedAt` | Long | Epoch timestamp of when the rating was saved |

> The snapshot is a point-in-time copy. The public TMDB rating shown in the Watched tab may be stale relative to current TMDB data — this is acceptable.

---

## Edge Cases

| # | Scenario | Expected Behavior |
|---|---|---|
| EC-1 | User taps a star and the app is force-killed before the DB write completes | Rating is lost. Acceptable — no durability guarantee required. |
| EC-2 | User opens the details screen while offline | Details screen loads from whatever data is available (existing behavior, unchanged). Rating action may still be attempted; DB is local so save should succeed regardless of connectivity. |
| EC-3 | User is on the Watched tab while offline | Full offline experience — all data served from local DB snapshot. |
| EC-4 | Watched tab is opened with no rated movies | Text-only empty state is displayed. |
| EC-5 | User switches to Watched tab while Discover is mid-load | Discover load is cancelled. On return to Discover, data reloads from scratch. |
| EC-6 | User navigates back to the movie list after rating | No visual change on the movie list item in the Discover tab. |
| EC-7 | User opens details of the same movie from both the Discover and Watched tabs | Both entry points lead to the same details screen with the correct rated/unrated state. |
| EC-8 | DB save fails on rating | Snackbar "Something went wrong. Please try again." Stars remain interactive. No entry written to DB. |
| EC-9 | Very long movie title in Watched tab list item | Title truncates with ellipsis; poster, ratings remain visible. |
| EC-10 | User rates 100+ movies | All loaded at once in Watched tab. No pagination. Performance acceptable for expected usage. |

---

## Out of Scope

- Changing or deleting a rating (ratings are immutable after first save).
- Filtering or sorting options in the Watched tab (future task).
- Pagination in the Watched tab (future task).
- Visual indicator on Discover tab list items showing a movie has been rated.
- Deep linking directly to a rated movie's details screen.
- Syncing ratings to a backend or across devices.
- Sharing ratings or making them public.
- Rating from the movie list (Discover tab) — rating is only available from the details screen.
- Pull-to-refresh on the Watched tab.
- Accessibility for the star rating input (TalkBack announcements on tap).
- Minimum touch target enforcement for the star row (implementation detail, not spec-level).

---

## Open Questions (Resolved)

| # | Question | Decision |
|---|---|---|
| OQ-1 | Rating format | 1–10 stars, tap Nth star selects 1–N gold, rest gray |
| OQ-2 | Can user re-rate? | No. Rating is immutable after first save. |
| OQ-3 | Tab name | "Watched" |
| OQ-4 | What data is stored locally? | Full movie snapshot at time of rating |
| OQ-5 | Watched tab list item design | Poster + title + TMDB rating + user rating (⭐ N) |
| OQ-6 | Sort order in Watched tab | Most recently rated first |
| OQ-7 | Empty state | Text only |
| OQ-8 | Error on DB save failure | Snackbar, revert UI, no retry action |
| OQ-9 | Force-kill before save completes | Rating lost, acceptable |
| OQ-10 | Back navigation after rating | Stay on details screen, no list item badge |
| OQ-11 | Deep linking | Out of scope |
| OQ-12 | Accessibility | Content description only on compact user rating icon |
| OQ-13 | Pagination | Out of scope, future task |
| OQ-14 | Discover scroll state on tab return | Restored |
| OQ-15 | Discover load on tab return | Cancelled and reloaded |
| OQ-16 | Pull-to-refresh on Watched tab | Not supported |
