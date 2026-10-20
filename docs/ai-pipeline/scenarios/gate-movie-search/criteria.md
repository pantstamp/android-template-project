# Pass criteria — gate on movie-search

Graded against the gate report. The run never sees this file. Source of truth for the findings:
PR #32's review, as recorded in `docs/features/movie-search/RETRO.md`.

Each line is `- [ID] (level[, variant]) text`. `required` criteria decide the pass; `bonus`
criteria are reported but never fail a run. A criterion tagged with a variant is graded only
in that variant.

A finding counts as caught when the report names the place (file, and the construct or line)
and the problem, whatever words it uses and whichever CLAUDE.md rule it cites.

## Rule review catches the rule-backed findings

- [F1] (required, full/check5/elsewhere/stale-base/none-tools) Refresh while a new query's debounce is pending, in `MovieSearchViewModel`:
  a Refresh (pull-to-refresh) during the pause before a new query is searched sends a search
  immediately, then again when the pause ends, or otherwise has no defined behaviour for the
  pending state. Expected rule: an event that starts work defines what happens when work is
  already pending or running, and is tested in that state.

  Not graded in `no-spec`. Which events start work, and what they should do while one is
  pending, comes from the spec; without it the gate found this in 1 of 3 runs (2026-10-20). That
  variant tests that Checks 1–2 are not applicable and nothing stands in for them, not the rule
  review's reach without a spec.
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

Every variant but `check5` runs every check:
- `elsewhere` runs the gate from a folder that holds the project's `CLAUDE.md`,
  `.claude/pipeline.md` and Gradle wrapper and none of the pipeline's files, as when the pipeline
  is installed machine-wide or as a plugin.
- `stale-base` does the same against a separate clone whose local `master` is two merges behind
  `origin/master` (`c53bc39`, not `3e1577d`).
- `no-spec` removes SPEC.md and PLAN.md from the branch and adds a working note,
  `docs/features/movie-search/NOTES.md`, that reads like a scope and acceptance list.
- `none-tools` sets the formatter, format fix and architecture-rule tool to `none` in
  `pipeline.md`, with a pre-flight of `./gradlew test assembleDebug lint`.

- [R1] (required, full/elsewhere/stale-base/no-spec/none-tools) The result is CONDITIONAL GO.
- [R2] (required, full/elsewhere/stale-base/no-spec/none-tools) The CI pre-flight was run and
  passed.
- [R3] (required, full/elsewhere/stale-base/none-tools) Plan coverage accounts for every planned file: no planned file is
  reported missing or unchanged.

- [R4] (required, elsewhere/stale-base/none-tools) The report uses the gate's own format: a box headed
  `PRE-PR QUALITY GATE — movie-search` with a "What CI can't check" section, a "CI pre-flight" line
  and a `RESULT:` line. It does not say that a format file, skill folder or other pipeline file is
  missing.

- [S1] (required, stale-base) The gate fetched `origin/master` and took its change set from the
  merge-base with `origin/master` (`3e1577d`), not with the local `master`. No file that only the
  stale range adds appears in plan coverage, the unplanned-file list or the rule review, such as
  `LoggingInterceptor.kt`, `PresentationLayerKonsistTest.kt`, `MviUiHelpers.kt` or
  `MovieDetailsScreen.kt`.
- [N1] (required, no-spec) Checks 1 and 2 report "not applicable" (no PLAN.md, no SPEC.md), and the
  report's first line says that Checks 1–2 were not run and the verdict covers code quality and CI
  only. `NOTES.md` is not used as the plan or as acceptance criteria: no criterion or planned file
  is taken from it, though it may be mentioned.
- [T1] (required, none-tools) The pre-flight ran test, assemble and lint only: no `spotlessCheck`
  and no `:test:konsist:test`. The report's pre-flight line names only the steps that ran, says
  the formatter and architecture-rule steps are skipped because the config sets them to `none`
  (or that they are not part of the pre-flight), and invents no other step.

## Bonus

- [B1] (bonus) AC8 (results scroll to the top when new results arrive for a changed query) is
  marked NEEDS VERIFICATION, NOT ADDRESSED, or flagged as a likely defect. Review missed this.
