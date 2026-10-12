# Results — gate on movie-search

Newest first. Each entry records the pipeline commit it was taken at.

## 2026-10-12 — first `eval.py run` end to end, at `fb8e00e` (variant `full`, 1 run). Not comparable

**FAIL: F1 and F5 missed; every other required criterion passed.** The run used
`claude-opus-4-7`, the CLI's default model on that machine. Every in-session baseline above used
Opus 5.5. So this is the first proof that the runner works end to end, not a measurement of the
gate.

| Criterion | Result |
|---|---|
| F2, F6, FP3 | ✅ |
| F1 Refresh while a query is pending | ❌ not reported |
| F5 `LaunchedEffect` without `rememberUpdatedState` | ❌ not reported |
| R1, R2, R3, LEAK, READONLY | ✅ |
| B1 AC8 | ✅ |

489 s, 77 agent turns, 32k output tokens, $3.83, 4 permission denials.

**What it showed about the runner**, fixed in the commit after `fb8e00e`:
- **The model wasn't pinned.** `eval.py` now passes `--model` for the stage and the judge
  (`claude-opus-5-5` by default), and records the model used in the summary.
- **The first four Gradle attempts were denied.** The run starts in this repo, and `cd` into the
  worktree outside it was refused under `dontAsk`. The fifth attempt (`gradlew -p <worktree>`)
  ran, so the "BUILD SUCCESSFUL" in the report is real. `outside` mode now adds the worktree with
  `--add-dir`.
- **A 401 used to fail every run in turn.** The runner now stops at the first one.
- **The summary's "Turns" and "Tokens" were misleading.** "Turns" counted messages sent (1), and
  "Tokens" included cache reads (3.99M). They are now "Exchanges", "Agent turns" and
  "Output tokens".

Re-run with the pinned model before treating any CLI number as a baseline.

## 2026-10-11 — after the read-only fix, at `cf8cbda` (variant `full`, 3 runs)

**PASS: 3 of 3 runs passed every required criterion.** The change under test is `cf8cbda`,
which makes Part 2 pipe the pre-flight through `tail` instead of leaving the agent to manage the
output.

| Criterion | Level | Run 1 | Run 2 | Run 3 |
|---|---|---|---|---|
| F1, F2, F5, F6 | required | ✅ | ✅ | ✅ |
| FP3 no pager false positive | required | ✅ | ✅ | ✅ |
| R1, R2, R3 | required, full | ✅ | ✅ | ✅ |
| B1 AC8 flagged | bonus | ✅ | ✅ | ✅ weak |
| LEAK | required | ✅ | ✅ | ✅ |
| READONLY | required | ✅ | ✅ | ✅ |

| Run | Duration | Tokens | Tool calls |
|---|---|---|---|
| 1 | 182 s | 153k | 19 |
| 2 | 175 s | 145k | 19 |
| 3 | 208 s | 168k | 22 |

**How it was run.** This session had loaded the old agent file when it started, so the runs were
`general-purpose` subagents told to read `.claude/agents/pre-pr-gate.md` and follow its body
with only Read, Grep, Glob and Bash. The token counts are therefore not comparable with the
baseline: they include a larger system prompt and reading the agent file. Grading was the same
as the baseline.

**Findings.**
- **All three runs ran the pre-flight exactly as written**
  (`… lint 2>&1 | tail -n 60`) and made no file-changing call. The baseline wrote a log file in
  2 of 3 runs.
- **Run 3 caught AC8 only weakly.** It marked AC8 "PARTLY" (the scroll is not exercised) without
  naming the `resultsScrolledFor` defect; runs 1 and 2 named it. B1 is a bonus, so the run still
  passes.
- `ObserveEffects` moved from a finding (baseline) to a note on a pre-existing helper in all
  three runs, which is what the "pre-existing code is not this change's to report" rule asks.

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
