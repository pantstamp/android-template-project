# Pass criteria — architect on movie-search

Graded against the committed `PLAN.md` and the session's dialogue. The run never sees this file.

The movie-search retro found that 5 of PR #32's 6 real review findings, and the most serious
defect found during implementation (A), originated in the original plan (`2ca290e`). Each has
since become a CLAUDE.md rule or an architect self-check. These criteria check that the plan now
avoids them. A criterion about a construct the plan does not use passes if the plan's design
makes the defect impossible (e.g. no `MutableSharedFlow` with `tryEmit` at all passes E2).

Each line is `- [ID] (level) text`. `required` criteria decide the pass; `bonus` criteria are
reported but never fail a run.

## PR #32's defects are designed out

- [E1] (required) Finding #1. For the search ViewModel, the plan defines what Refresh and Retry
  do when a search is already pending (a new query waiting for its debounce) or running, and
  what a query change does while a refresh or retry is running. It plans a test for each of
  those states. Defining Refresh only by whether results are on screen fails this criterion.
- [E2] (required) Finding #2. Every `MutableSharedFlow` the plan feeds with `tryEmit` declares
  an explicit `onBufferOverflow`.
- [E3] (required) Finding #5. Every long-running effect in the plan (a `LaunchedEffect`
  collecting a flow, e.g. the field's `snapshotFlow`) that calls a callback parameter reads it
  through `rememberUpdatedState`.
- [E4] (required) Finding #6. `MovieRow`, and anything else used by more than one screen, is
  planned into a shared package (`presentation/uicomponent` or equivalent), not imported from
  one screen's package by another.
- [E5] (required) Finding #4. The plan has one shared `LoadError` → message mapping for the
  list screens (Discover, its Toasts, search), not one per screen. Details may keep its own,
  because its generic text refers to a single movie.
- [E6] (required) Defect A. The query pipeline does not rely on a `StateFlow` to deliver every
  change: a change and its reversal ("matrix" → "" → "matrix") made before the collector
  resumes still starts a new search. A generation counter, a buffered `SharedFlow`, a `Channel`
  or an equivalent is used, or the plan explains why conflation cannot lose a needed search.

## The plan's general shape

- [G1] (required) Every phase ends with a verification command.
- [G2] (required) Every phase that touches a composable or `res/` includes `lint` in its
  verification command.
- [G3] (required) Every new screen state (search loading, results, empty, offline, generic
  error, loading over results; Details offline and generic error; the poster placeholder) has a
  planned `@Preview`.
- [G4] (required) Each string or drawable resource is added in the phase that first uses it.
- [G5] (required) The plan contains a target-state diagram, a state diagram for the search
  ViewModel, and a decision record that lists rejected alternatives.

## The session

- [S1] (required) The architect asked for the user's design before revealing its own, and
  briefed the current state (with a diagram) before asking.
- [S2] (required) The plan honours the user's scripted decisions: the UI owns the field text;
  a separate search ViewModel; a new API model for `GET movie/{id}`; Details reads the database
  first, then the network, and never writes.

The runner adds `COMMITTED` (`PLAN.md` was committed on the branch), `STAMP` (`pipeline: vX.Y.Z`
under the title, matching `.claude/PIPELINE_VERSION`) and `LEAK` (the run reached for `master`, a
remote, the original plan's commit or the retro) itself.

## Bonus

- [B1] (bonus) The plan flags the AC8 edge case: after clearing a query and typing the same
  one again, the results still scroll to the top (or it records a decision about it).
