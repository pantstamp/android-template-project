# Results — gate on movie-search

Newest first. Each entry records the pipeline commit it was taken at.

## 2026-10-11 — first suite baseline, at `409da9a` (variant `full`, 3 runs)

**FAIL on `READONLY`; every judged criterion passed 3 of 3.**

| Criterion | Level | Run 1 | Run 2 | Run 3 |
|---|---|---|---|---|
| F1 Refresh while a query is pending | required | ✅ | ✅ | ✅ |
| F2 `reruns` without `onBufferOverflow` | required | ✅ | ✅ | ✅ |
| F5 `LaunchedEffect` without `rememberUpdatedState` | required | ✅ | ✅ | ✅ |
| F6 `MovieRow` imported across screens | required | ✅ | ✅ | ✅ |
| FP3 no pager false positive | required | ✅ | ✅ | ✅ |
| R1 CONDITIONAL GO | required, full | ✅ | ✅ | ✅ |
| R2 pre-flight run and passed | required, full | ✅ | ✅ | ✅ |
| R3 every planned file accounted for | required, full | ✅ | ✅ | ✅ |
| B1 AC8 flagged | bonus | ✅ | ✅ | ✅ |
| LEAK `RETRO.md` not touched | required | ✅ | ✅ | ✅ |
| READONLY no file-changing call | required | ❌ | ✅ | ❌ |

| Run | Duration | Tokens | Tool calls |
|---|---|---|---|
| 1 | 228 s | 112k | 23 |
| 2 | 209 s | 116k | 22 |
| 3 | 252 s | 114k | 26 |

**How it was run.** `claude -p` returned 401 from inside the desktop session, so the three runs
were spawned as `pre-pr-gate` subagents with prompts from `eval.py setup`, in parallel, one
worktree each. The judged criteria were graded by hand against `criteria.md`. `LEAK` and
`READONLY` were computed by `eval.py`'s own checks on the subagent transcripts.

**Findings.**
- **The gate breaks its own read-only rule.** Runs 1 and 3 redirected the Gradle output into a
  log file (`./gradlew … > …/preflight.log 2>&1`), which "Ground rules" forbids by name
  ("redirects into files"). Both files were outside the repo, so no damage was done, but the
  rule is not holding. The likely cause is that Part 2 does not say how to keep a long Gradle
  output out of context. A fix belongs in `.claude/agents/pre-pr-gate.md` Part 2, e.g. "pipe
  through `tail`; never redirect to a file". That is a pipeline change, so it is measured against
  this baseline.
- **AC8 was caught in all 3 runs; review had missed it.** Runs 2 and 3 named the cause: the
  `resultsScrolledFor` guard is not reset when the query is cleared.
- **Beyond PR #32's findings,** every run also reported:
  - `ObserveEffects` not using `rememberUpdatedState`, which the retro noted as pre-existing;
  - a missing `@Preview` for "no results" while a search is pending (runs 1 and 2);
  - `MovieSearchResults` imported by `DiscoverScreen` without being named like a screen. The
    retro's Konsist rule later found this and renamed the class `MovieSearchScreen`.
  None of these is a false positive.
- Compared with the PR #35 single run (4 of 4, about 4 min and 145k tokens): the same catch rate,
  about the same duration, and fewer tokens per run.
