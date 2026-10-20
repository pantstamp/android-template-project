# Pass criteria — gate on movie-search

Graded against the gate report. The run never sees this file. Source of truth for the findings:
PR #32's review, as recorded in `docs/features/movie-search/RETRO.md`.

Each line is `- [ID] (level[, variant]) text`. `required` criteria decide the pass; `bonus`
criteria are reported but never fail a run. A criterion tagged with a variant is graded only
in that variant.

A finding counts as caught when the report names the place (file, and the construct or line)
and the problem, whatever words it uses and whichever CLAUDE.md rule it cites.

## Rule review catches the rule-backed findings

- [F1] (required) Refresh while a new query's debounce is pending, in `MovieSearchViewModel`:
  a Refresh (pull-to-refresh) during the pause before a new query is searched sends a search
  immediately, then again when the pause ends, or otherwise has no defined behaviour for the
  pending state. Expected rule: an event that starts work defines what happens when work is
  already pending or running, and is tested in that state.
- [F2] (required) The `reruns` `MutableSharedFlow` in `MovieSearchViewModel` is used with
  `tryEmit` but has no explicit `onBufferOverflow`, so a request can be silently dropped.
- [F5] (required) The `LaunchedEffect` in `DiscoverScreen` that calls `onSearchEvent` reads the
  callback without `rememberUpdatedState`.
- [F6] (required) `MovieSearchResults` imports `MovieRow` (or another building block) from the
  `movielist` screen's package instead of a shared `presentation/uicomponent` package.

## No false positive

- [FP3] (required) The report does not claim that `HorizontalPager` in
  `MovieCatalogTabbedScreen` drops or recreates the Discover page because of a default
  `beyondViewportPageCount`, nor that a comment about the pager keeping Discover composed is
  wrong. That pager sets `beyondViewportPageCount = 1` on a line outside the diff.

## Full run

`full` and `elsewhere` both run every check; `elsewhere` runs the gate from a folder that holds the
project's `CLAUDE.md`, `.claude/pipeline.md` and Gradle wrapper and none of the pipeline's files,
as when the pipeline is installed machine-wide or as a plugin.

- [R1] (required, full/elsewhere) The result is CONDITIONAL GO.
- [R2] (required, full/elsewhere) The CI pre-flight was run and passed.
- [R3] (required, full/elsewhere) Plan coverage accounts for every planned file: no planned file is
  reported missing or unchanged.

- [R4] (required, elsewhere) The report uses the gate's own format: a box headed
  `PRE-PR QUALITY GATE — movie-search` with a "What CI can't check" section, a "CI pre-flight" line
  and a `RESULT:` line. It does not say that a format file, skill folder or other pipeline file is
  missing.

## Bonus

- [B1] (bonus) AC8 (results scroll to the top when new results arrive for a changed query) is
  marked NEEDS VERIFICATION, NOT ADDRESSED, or flagged as a likely defect. Review missed this.
