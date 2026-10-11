# Results — architect on movie-search

Newest first. Each entry records the pipeline commit it was taken at.

## 2026-10-11 — after the event × state change, at `59be352` (3 runs)

**WARN: 2 of 3 runs passed every required criterion.** E1 went from 0 of 3 to 3 of 3. Run 2
failed E3.

`59be352` made the architect plan a ViewModel's event tests as an event × state table, with a
test named in every cell where work is pending or running. Before, the instruction was "a test
per state that matters".

| Criterion | Level | Run 1 | Run 2 | Run 3 |
|---|---|---|---|---|
| E1 events defined and tested in every busy state | required | ✅ | ✅ | ✅ |
| E2 `tryEmit` without `onBufferOverflow` | required | ✅ | ✅ | ✅ |
| E3 `rememberUpdatedState` in long-running effects | required | ✅ | ❌ | ✅ |
| E4 shared composables in `uicomponent` | required | ✅ | ✅ | ✅ |
| E5 one shared `LoadError` mapping | required | ✅ | ✅ | ✅ |
| E6 no `StateFlow` conflation of A → B → A | required | ✅ | ✅ | ✅ |
| G1–G5 shape | required | ✅ | ✅ | ✅ |
| S1 briefing first, user's design asked for first | required | ✅ | ✅ | ✅ |
| S2 scripted decisions honoured | required | ✅ | ✅ | ✅ |
| COMMITTED, LEAK | required | ✅ | ✅ | ✅ |
| B1 AC8 clear-and-retype edge case | bonus | ✅ | ❌ | ❌ |

| Run | Plan | Event × state table | Busy-cell policy | Agent tokens |
|---|---|---|---|---|
| 1 | 1,082 lines | 6 events × 7 states | Retry/Refresh run now while pending, ignored while running | 216k |
| 2 | 1,226 lines | 5 events × 7 states | Retry/Refresh cancel and run the current query now | 237k |
| 3 | 1,066 lines | 6 events × 6 states | Retry/Refresh ignored while pending or running | 234k |

Run the same way as the baseline: subagents, the orchestrating session as the user from
`answers.md`, and one judge agent per plan with the same prompt.

**Findings.**
- **The table changed the designs, not just the tests.** Filling every cell surfaced
  defects that a list of tests would not have:
  - **Run 1 found a stuck indicator.** Type a different query, return to the one on screen,
    then pull to refresh and have it fail. It fixed this, and named the cell's test.
  - **Run 2 changed its own design.** As revealed, a Refresh during a pending new query kept
    the old results and showed a Toast on failure: PR #32's finding #1, in a new form. Writing
    the cells made it fail to the full-screen error instead, and the run raised the change for
    a decision.
  - **Run 3 named a test in all 18 busy cells,** including the cells whose answer is "ignored".
- **Run 2's E3 failure is real.** Its `DiscoverScreen` passes `onMovieClicked` straight into
  two `ObserveEffects` handlers, without `rememberUpdatedState`. It even listed `ObserveEffects`'
  capture for separate triage, and claimed the handlers close over only stable values. Runs 1
  and 3 wrapped the callback. The architect's code-block self-check ("Is every code block in the
  plan held to CLAUDE.md?") did not catch it.
  - A caveat on judge consistency: in the baseline, run 1 had the same pattern and passed E3,
    with a caveat in the evidence. It may have been under-graded then.
- **Cost is unchanged:** 216k–237k agent tokens per run, against 221k–237k for the baseline.
- **The three plans** are kept outside the repo for this session only, in the scratchpad's
  `arch2/plans/`.

**Possible next change.** Add `ObserveEffects` handlers to the architect's code-block
self-check, or fix the helper. The retro already noted `ObserveEffects` as a candidate for its
own fix: wrapping `onEffect` in `rememberUpdatedState` once inside the helper removes this
failure for every caller.

## 2026-10-11 — first baseline, at `99fa092` (3 runs)

**FAIL on E1 in all 3 runs; every other required criterion passed 3 of 3.** The "before" is the
original plan (`2ca290e`), which according to the retro designed in all six defects E1–E6.

| Criterion | Level | Run 1 | Run 2 | Run 3 |
|---|---|---|---|---|
| E1 events defined and tested in every busy state | required | ❌ | ❌ | ❌ |
| E2 `tryEmit` without `onBufferOverflow` | required | ✅ | ✅ | ✅ |
| E3 `rememberUpdatedState` in long-running effects | required | ✅ | ✅ | ✅ |
| E4 shared composables in `uicomponent` | required | ✅ | ✅ | ✅ |
| E5 one shared `LoadError` mapping | required | ✅ | ✅ | ✅ |
| E6 no `StateFlow` conflation of A → B → A | required | ✅ | ✅ | ✅ |
| G1 verification command per phase | required | ✅ | ✅ | ✅ |
| G2 `lint` in UI phases | required | ✅ | ✅ | ✅ |
| G3 a `@Preview` per new screen state | required | ✅ | ✅ | ✅ |
| G4 resources added where first used | required | ✅ | ✅ | ✅ |
| G5 diagrams and decision record | required | ✅ | ✅ | ✅ |
| S1 briefing first, user's design asked for first | required | ✅ | ✅ | ✅ |
| S2 scripted decisions honoured | required | ✅ | ✅ | ✅ |
| COMMITTED | required | ✅ | ✅ | ✅ |
| LEAK | required | ✅ | ✅ | ✅ |
| B1 AC8 clear-and-retype edge case | bonus | ✅ | ❌ | ❌ |

| Run | Plan | Phases | Search engine | Agent tokens |
|---|---|---|---|---|
| 1 | 897 lines | 5 | tracked `Job` + `delay`; Retry and Refresh ignored while busy | 221k |
| 2 | 1,135 lines | 6 | tracked `Job` + `delay`; Retry and Refresh cancel and restart | 229k |
| 3 | 1,219 lines | 5 | `StateFlow` of requests with a generation counter | 237k |

Each run took 7 to 8 turns and about 14 minutes of agent time.

**How it was run.** `claude -p` returned 401 from inside the desktop session, so each run was a
`general-purpose` subagent told to follow the worktree's `.claude/skills/architect/SKILL.md`,
with today's pipeline committed into a worktree at `bdfacd9` by `eval.py setup`. The simulated
user was the orchestrating session, which answered strictly from `answers.md` through
`SendMessage`. Each plan was graded by a separate judge agent that read only `criteria.md` and
the plan. S1 was graded from the dialogue. `LEAK` and `COMMITTED` came from `eval.py`'s checks.

**Findings.**
- **Five of the six plan-borne defects are gone in every run.** E2–E6 all pass 3 of 3. All
  three runs reached for the right mechanism unprompted. Run 3 caught the `SharedFlow`
  subscription race while writing the code, and switched to a generation-counted `StateFlow`.
- **PR #32's finding #1 itself is fixed in every run.** Each plan defines Refresh during a
  new query's debounce, and tests it:
  - run 1, `shouldIgnoreRefreshWhileNewQueryIsPending`;
  - run 2, `shouldTreatRefreshDuringPendingNewQueryAsNewQuery`;
  - run 3, `shouldShowFullScreenErrorWhenRefreshDuringPendingNewQueryFails`.
- **What fails E1 is the full matrix.** No run plans a test for every busy state: Retry while
  pending or running, Refresh during an in-flight search or refresh, a query change during a
  Retry. The behaviour is *defined* in each plan (ignore, or cancel and restart). The tests
  cover only the case the retro named. The architect's self-check asks for "a test per state
  that matters", and the runs read "that matters" narrowly.
- **The new design-session flow (PR #36) held.** In every run:
  - the briefing (diagram, closest equivalent, rules, weaknesses, open questions) came before
    any design;
  - the architect said once that designing first is where the learning is;
  - it reviewed the user's changes decision by decision.

  Two runs conceded a scripted change was better than their own design, and named the defect
  theirs had: Discover's effects queuing while its screen is out of composition.
- **Leak check refined.** The first leak check flagged runs 1 and 2 for reading
  `discover-load-error-recovery/RETRO.md`, which is an earlier feature's retro and legitimately
  in the snapshot. `forbidden_paths` now match the full path.

**Open question for the next entry.** Is E1 right as written? Either:
- the architect's "a test per state that matters" self-check should be tightened, so that the
  plan carries the whole event × state matrix as tests; or
- E1 should be split into the retro's finding (passes 3 of 3) and the full matrix (fails 3 of
  3), so that the scenario separates a regression from a gap that already exists.
