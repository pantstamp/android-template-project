# Results — gate on movie-search

Newest first. Each entry records the pipeline commit it was taken at.

## 2026-10-23 — 3.0.0 plumbing check, the plugin, at `738f417` (2 variants, 1 run each)

**PASS: `full` 1 of 1, `elsewhere` 1 of 1**, on `claude-opus-5-5`. A packaging-only release, run
on the rule for one (suite README, "Status"): one run of each variant that could break. Both
loaded the plugin from the run's frozen copy and ran as `--agent ai-pipeline:pre-pr-gate`; every
criterion passed, B1 included. `elsewhere` now means a project that installs the plugin: only
`CLAUDE.md`, `pipeline.md` and the Gradle wrapper on disk, with no `--agents` stand-in.

| Variant | Duration | Agent turns | Output tokens | Cost | Denials |
|---|---|---|---|---|---|
| `full` | 210 s | 28 | 17,846 | $1.46 | 1 |
| `elsewhere` | 200 s | 24 | 17,597 | $1.45 | 1 |

- **The denial in each run is the known `W=…; git -C $W` shell variable** (HANDOFF, "Things that
  bit us"); harmless.
- **Both runs' first event listed the six skills under their bare names as well** as `ai-pipeline:`.
  The later runs and every attempt to reproduce it did not; likely a list cached by the desktop
  session the runs were started from, which began before the move. The gate has no Skill tool, so
  it cannot have mattered.

## 2026-10-20 — 2.1.0, built in four slices, at `4f73a70` … `923200a` (6 variants, 3 runs each)

**PASS: every variant 3 of 3**, on `claude-opus-5-5`, after one re-run (`stale-base`). Five new or
changed variants test the fixes from the step-4 dry run (`V2.1.0_FIXES.md`):

| Variant | Tests | Slice a | Slice b | Slices c+d |
|---|---|---|---|---|
| `full` | the gate as usual | 3 of 3 | 3 of 3 | 3 of 3 |
| `elsewhere` | pipeline installed outside the project (entry 1) | 3 of 3 | 3 of 3 | — |
| `stale-base` | local base two merges behind `origin` (entry 2) | — | 2 of 3, re-run 3 of 3 | — |
| `no-spec` | no SPEC.md / PLAN.md, a look-alike note (entry 4) | — | 3 of 3 without F1 | — |
| `none-tools` | formatter and architecture tool `none` (entry 7) | — | 3 of 3 | — |

- **What each fix changed held in every run:** R4 (the report keeps its format with no pipeline
  files nearby), S1 (fetched, diffed against `origin/master`, no file from the stale range), N1
  (Checks 1–2 not applicable, the caveat line, the note not used), T1 (no formatter or Konsist
  step, none invented).
- **`stale-base` run 2 missed F1 once;** the re-run passed 3 of 3, and F1 passed all 9 runs of
  `full`, `elsewhere` and `none-tools` in the same slice.
- **F1 is not graded in `no-spec`.** Without a spec the gate found it in 1 of 3 runs: which events
  start work, and what they do while one is pending, comes from the spec. The variant tests entry
  4, not the rule review's reach without a spec. Recorded here as a limit of the gate on work done
  without the earlier stages.
- `no-spec` runs took half as long (100–160 s against about 220 s): there is no plan to check.

## 2026-10-15 — step 3, project facts in `.claude/pipeline.md`, at `32f5662` (variant `full`, 3 + 3 runs)

**PASS on the re-run: 3 of 3 runs passed every required criterion**, bonus included, on
`claude-opus-5-5`. The first batch was **WARN, 2 of 3**: run 2 reported no finding for F5 (the
`LaunchedEffect` that captures `onSearchEvent`); every other criterion passed in all three runs.
`pipeline-v1.1.0`, run the same day for comparison, caught F5 in 3 of 3. F5 has now been missed in
1 of 15 runs across all batches, so the miss is read as variance, not as the change.

The prompt changed with the config: the agent uses its working directory for `CLAUDE.md` and
`.claude/pipeline.md`, and replaces `{{base}}` (and the config's `master`) with the fixed base.

| Run | Duration | Agent turns | Output tokens | Cost | Denials |
|---|---|---|---|---|---|
| 1 | 218 s | 26 | 21,770 | $1.78 | 1 |
| 2 | 219 s | 35 | 21,500 | $1.73 | 1 |
| 3 | 197 s | 28 | 17,512 | $1.21 | 1 |

**Earlier on 2026-10-14, the first attempt at step 3** (a platform profile, `1d62a57`) passed
3 of 3. The gate was the one stage that attempt did not regress; the attempt was replaced, see
the roadmap's §5.

**The same-day `v1.1.0` comparison failed R1 and R2 for a setup reason:** its checkout had no
`local.properties`, so Gradle stopped at "SDK location not found". Copy the `sdk.dir` line into a
comparison checkout before running it.

## 2026-10-12 — official `eval.py` baseline, at `7acd123` (variant `full`, 3 runs)

**PASS: 3 of 3 runs passed every required criterion**, bonus included, on `claude-opus-5-5`.

| Criterion | Level | Run 1 | Run 2 | Run 3 |
|---|---|---|---|---|
| F1 Refresh while a debounce is pending | required | ✅ | ✅ | ✅ |
| F2 `tryEmit` without `onBufferOverflow` | required | ✅ | ✅ | ✅ |
| F5 `LaunchedEffect` captures a stale callback | required | ✅ | ✅ | ✅ |
| F6 shared composable outside `uicomponent` | required | ✅ | ✅ | ✅ |
| FP3 no false finding on the pager | required | ✅ | ✅ | ✅ |
| R1–R3 verdict, pre-flight, plan check | required | ✅ | ✅ | ✅ |
| LEAK, READONLY | required | ✅ | ✅ | ✅ |
| B1 AC8 marked as needing UI verification | bonus | ✅ | ✅ | ✅ |

| Run | Duration | Agent turns | Output tokens | Cost | Denials |
|---|---|---|---|---|---|
| 1 | 211 s | 32 | 19,951 | $1.83 | 1 |
| 2 | 205 s | 37 | 20,248 | $1.81 | 1 |
| 3 | 237 s | 32 | 18,804 | $1.70 | 1 |

## 2026-10-12 — first passing `eval.py run`, at `2625a1b` (variant `full`, 1 run)

**PASS: 1 of 1.** Every criterion passed, bonus included, on `claude-opus-5-5`: 218 s, 37 agent
turns, 21k output tokens, $1.64. This is the first result taken by the runner rather than
in-session, and it agrees with the in-session baseline.

One denial remains, and the agent caused it: it used a shell variable despite the prompt, was
refused, and rewrote the command. It is not a setup problem.

## 2026-10-12 — `eval.py run` with the pinned model, at `54886cb` (variant `full`, 1 run). Setup issue

**FAIL on R2 only: the pre-flight was refused, not failed.** Every rule-review criterion passed on
`claude-opus-5-5`, F1 and F5 included. So the misses in the run below were the model.

| Model | Duration | Agent turns | Output tokens | Cost | Denials |
|---|---|---|---|---|---|
| claude-opus-5-5 | 183 s | 32 | 17k | $1.82 | 3 |

The three denials were a compound git inspection using `W=…` and `$(…)`, and two Gradle
invocations: `cd <worktree> && ./gradlew …`, and the worktree's own `gradlew` by absolute path.
Tested directly with `claude -p` under `dontAsk`:
- **Refused, even with `--add-dir` and a matching allow rule:** `cd` into a directory outside the
  working directory, and command substitution.
- **Allowed by the existing rules:** `./gradlew -p <worktree>`, run from the repo.

The scenario's `prompt.md` now gives those forms, as it already did for `git -C`. The gate agent
is unchanged: in an interactive session these commands simply ask for permission.

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
