# AI pipeline improvements — agents for bounded work

Status: **proposed, not started.** Nothing in this document has been implemented yet.

## Why

The pipeline runs six skills in the main conversation. That works for the stages that are
conversations: interviews, outline approval, per-phase checkpoints, triage decisions. It works
less well for two kinds of work:

- **Bulky work that fills the context.** A feature's implementation runs over several phases,
  each with file reads, gradle output and fix loops, all in one session. The `/compact` and
  `/clear` advice in `developer/SKILL.md` and `docs/AI_WORKFLOW.md` is a manual workaround for
  this.
- **Judgement that should be independent.** The pre-PR gate currently runs in the same context
  that wrote the code, so it is checking its own work. That matters most where the gate makes a
  judgement call: whether an acceptance criterion is really covered.

There is also a gap that no stage covers. Every rule in CLAUDE.md's "Flow and coroutines" section
came from a review finding that compiled and passed all CI checks. Once a rule is written down,
nothing checks the next diff against it before `@claude-review`, which runs after the PR is open
and is paid per run. A known mistake can reach review twice.

## Design rule

**Skills own the conversation and the decisions. Agents do bounded work and return a report.**

Every `/slash` entry point stays a skill. A skill may delegate a bounded job to an agent defined
in `.claude/agents/`, then presents the agent's report and handles everything that needs the
user. Agents never commit, never ask the user anything, and never make decisions the user owns.

## Scope

| # | Change | Gain |
|---|---|---|
| 1 | Pre-PR gate runs as an agent | Independent judgement on acceptance criteria; gradle output kept out of the main context |
| 2 | New gate Check 5: review the diff against CLAUDE.md's Flow, Compose and MVI rules | Stops a mistake already recorded as a CLAUDE.md rule from reaching review again |
| 3 | Manual verification is recorded in phase commits | Keeps a context-free gate from downgrading manually verified criteria |
| 4 | Developer implements each phase through a Sonnet agent | Main context stays small for the whole feature; Sonnet without a manual `/model` switch |
| 5 | Docs updated | `AI_WORKFLOW.md` and `skills/README.md` describe the new shape |

Out of scope: an explorer agent for the architect stage. That session is short and its exploration
is entangled with design decisions, so the gain is marginal. Revisit only as on-demand targeted
questions.

---

## Change 1 — Pre-PR gate agent

**New file: `.claude/agents/pre-pr-gate.md`**

- `model: inherit`: judging criteria coverage is the gate's whole value.
- `tools: Read, Grep, Glob, Bash`. No `Edit` or `Write`: the gate reports problems and can't
  fix its way to GO. `Bash` is still needed for git and gradle; that it never modifies files is
  an instruction in the agent body. Check whether the agent `tools` field accepts Bash patterns
  (e.g. `Bash(git:*)`, `Bash(./gradlew:*)`) to enforce it.
- Body: the current Part 1 (Checks 1–4) and Part 2 (CI pre-flight) move here from the skill,
  plus Check 5 (Change 2) and the manual-verification lookup (Change 3).
- Input: the feature name only. **Not** the developer session's account of what it built:
  independence is the point.
- Output: the report in `pre-pr-checklist/REPORT_FORMAT.md`, plus the raw failure output for
  any pre-flight task that failed.
- On a `spotlessCheck` failure it reports it; it does not run `spotlessApply`.

**Edit: `.claude/skills/pre-pr-checklist/SKILL.md`** becomes a thin wrapper:

1. Resolve the feature name (from the input or the branch name) and confirm.
2. Spawn `pre-pr-gate` in the foreground and wait for the report.
3. Show the report.
4. Follow-ups happen in the main conversation, under the same limits as today:
   - offer fixes for what can be fixed
   - run `spotlessApply` on a Spotless failure
   - never change a Konsist rule or add a lint baseline/suppression without asking
5. Re-run by spawning the agent again. On CONDITIONAL GO, tell it which checks to re-run, which
   keeps "re-run only what failed" working.
6. GO → offer the developer skill's PR step, as today.

**Edit: `REPORT_FORMAT.md`**: add a "Rule review" line under "What CI can't check" for Check 5.

## Change 2 — Check 5: review the diff against CLAUDE.md

Added to Part 1 of the gate.

**Scope**: added and changed lines in the change set, read with enough surrounding code to judge
them. Pre-existing code in a touched file is not this change's to report.

**Rules checked**: CLAUDE.md's judgement rules, the ones the toolchain cannot check:
- **Flow and coroutines**:
  - `emitAll` vs nested collect
  - no collect inside a collector
  - Room flows collected from lifecycle events
  - flags reset on every exit path
  - injected clock
  - user-facing strings out of ViewModels
  - `StateFlow` used as an event stream
  - `tryEmit` without `onBufferOverflow`
  - events arriving while work is pending or running
- **Compose**:
  - resources read through Compose
  - a `@Preview` per visual state
  - no Koin in presentation composables
  - `rememberUpdatedState` in long-running effects
  - shared composables in `uicomponent`
- **MVI**:
  - neutral `UiState` defaults, with `initialState` for the start state
  - no mutable state exposed
  - Event/SideEffect in separate files
  - one-time effects through `setEffect`

Do not restate Konsist- or lint-enforced rules; Part 2 runs those.

**Each finding states**:
- `file:line`
- the CLAUDE.md rule it breaks, named or quoted
- the failure scenario (inputs or sequence → wrong outcome), or, if none is reachable today, what
  currently prevents it and what change would remove that protection
- a suggested fix

**A finding does not need a failure reachable today.** Matching a rule's pattern is enough: the
rules exist because "safe today" was safe by accident. (A first draft required a reachable
failure. In verification it led the gate to examine three known review findings and leave all
three out of the report.)

**False-positive guard** (from the review prompt): before claiming a library default applies,
check the call site and the surrounding file for an override.

**Effect on the result**: findings make the result at least **CONDITIONAL GO**. Each one is the
user's decision to fix or dismiss, as in review triage. Check 5 never makes NO GO by itself: it
is a static reading and can be wrong.

**Relationship to `@claude-review`**: it does not replace it. Check 5 can only enforce rules that
are already written down, so it catches **repeats of known mistakes, not new kinds**; discovering
those stays the review's job. The goal is that review findings are new kinds of mistake, and that
the retrospective's "repeat of a finding already recorded" count stays at zero.

## Change 3 — Record manual verification in phase commits

The gate counts a criterion as COVERED when the user verified it manually. Those checks happen at
developer checkpoints, in a conversation the gate agent can't see.

**Edit: `.claude/skills/developer/SKILL.md`, step 2e.** When the user reports a manual check at the
checkpoint (on a device, emulator or preview), record it in the phase commit as a trailer:

```
feat(user-profiles): profile screen

Verified-manually: empty state renders when no genres are selected (emulator, API 34)
```

**Gate agent, Check 2**: read `git log master..HEAD --format=%B`. A criterion matched by a
`Verified-manually:` trailer is COVERED (manual). The agent stays independent: it reads a durable
record, not the session.

## Change 4 — Developer per-phase agent (Sonnet)

**New file: `.claude/agents/phase-implementer.md`**

- `model: sonnet`; default (full) tools.
- Input: the feature name and phase number, plus the user's feedback when continuing a rejected
  phase.
- It reads CLAUDE.md, PLAN.md and SPEC.md itself.
- It implements exactly what the phase specifies, including the optional "prove a regression test
  can fail" step, then runs the phase's verification command, fixing and re-running on failure.
- Hard rules:
  - **Never commit.**
  - **Never improvise around the plan.** If something in the plan looks wrong, or a fix would
    change its design, stop and return `BLOCKED: <question>` with what was done so far.
  - **No device or emulator checks.** Those belong to the user at the checkpoint.
- Output, using step 2c's report format:
  - files created and modified
  - decisions the plan left open
  - deviations and their reason
  - the verification command, with the tail of its output

**Edit: `.claude/skills/developer/SKILL.md`, step 2** becomes:

1. Spawn `phase-implementer` for phase N in the foreground.
2. **Verify the report; don't take it on trust:**
   - `git status --short` against the phase's file list in PLAN.md
   - re-run the phase's verification command (gradle's up-to-date checks keep this fast)
3. Report to the user and wait for approval (the checkpoint, unchanged).
4. If the user asks for changes, continue the **same** agent with SendMessage and the feedback, so
   it keeps its context. Then go back to step 2.
5. On `BLOCKED`, put the question to the user, then continue the same agent with the answer.
6. On approval, commit, with any `Verified-manually:` trailers (Change 3).

Section 3 (context management) shrinks to a resume note: the main context now holds only reports,
and a new session resumes from `git log --oneline master..HEAD`.

Steps 1 (branch), 4 (gate offer) and 5 (PR) stay in the skill unchanged.

## Change 5 — Docs

- **`docs/AI_WORKFLOW.md`**:
  - Stage 3:
    - remove the `/model sonnet` tip
    - shrink "If the context window fills up"
    - explain that each phase runs in an agent and what the checkpoint now verifies
    - mention `Verified-manually:` trailers
  - Stage 4:
    - the gate runs as an independent agent
    - add Check 5 to the CI-vs-gate table
- **`.claude/skills/README.md`**: add an "Agents" table (agent, used by, model, why), and the
  design rule above next to the existing "Maintaining these skills" rule.
- **`README.md:118`**: "five Claude Code skills" → "six".

---

## Order and verification

1. **Changes 1 + 2 + 3 (gate side).** Gate first: it is the simplest, and Check 5 is the
   highest-value addition.
   **Verify:**
   - Check out the movie-search branch as it was when the PR was opened (before
     `86121a5 fix(movie-search): address review feedback on PR #32`) in a worktree.
   - Run the gate there and count how many of PR #32's review findings, listed in
     `docs/features/movie-search/RETRO.md`, Check 5 catches.
   - Compare the rest of the report with what the old skill would have produced.
   **Result** (snapshot `dd348bf`, base `3e1577d`; PR #32 had 6 real review findings):
   - **Run 1: full gate, with the snapshot's own CLAUDE.md.** CONDITIONAL GO; pre-flight passed.
     - Check 5 caught 0 of the 6, because none of their rules existed yet. That is expected: Check 5
       enforces written rules only.
     - Check 2 found a likely AC8 defect that review missed (re-typing a cleared query does not
       scroll to the top). It is still on `master`, unconfirmed on a device.
   - **Run 2: Check 5 only, with the current CLAUDE.md.** Reported 1 finding (#6). The other three
     findings that have rules (#1, #2, #5) were seen but left out, because the first draft required
     a failure reachable today.
   - **Run 3: Run 2 after removing that requirement.** Caught **4 of 4** rule-backed findings (#1,
     #2, #5, #6). It also raised three more for the user to judge:
     - Retry during a load is not tested in Details
     - `ObserveEffects` lacks `rememberUpdatedState`; the retro had noted this as pre-existing
     - two empty-result states have no preview

     The two findings without a rule (#4, #7) were not caught, as expected.
2. **Change 3 (developer side) + Change 4.**
   **Verify:** run a small throwaway plan on a scratch branch end to end. That run should cover:
   - one approved phase
   - one rejected phase continued via SendMessage
   - one `BLOCKED` return
3. **Change 5.** **Verify:** read-through; no stale references to `/model sonnet` or to the gate
   running in-context.

Work on a `chore/agent-pipeline` branch.

## Open items

- ~~Does the agent `tools` field accept Bash patterns?~~ **No** — `tools` is a plain list
  (`disallowedTools` also exists). The gate's allowlist excludes `Edit`/`Write`; that its Bash use
  never modifies files is enforced by instruction only.
- ~~Do subagents load CLAUDE.md?~~ **Yes**, by default (`omitClaudeMd: true` turns it off).
- SendMessage continuing a completed subagent is documented; confirm it in practice during the
  Change 4 verification run.
- Custom agents in `.claude/agents/` appear to be loaded at session start (observed while
  implementing Change 1): a session that predates a new agent file could not spawn it by name.
  Restart the session after adding one.
- Whether the explorer agent comes back later as on-demand targeted questions for the architect.
