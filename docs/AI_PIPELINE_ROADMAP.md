# AI pipeline roadmap — from this project to a reusable package

Status: **step 1 done** (PR #35, [`docs/features/agent-pipeline/IMPROVEMENTS.md`](features/agent-pipeline/IMPROVEMENTS.md)).
**Step 2 is next**; its brief is [§4](#4-step-2-brief--golden-scenarios).

**Starting a new session on this roadmap:**
- Read this file, then `CLAUDE.md`, `docs/AI_WORKFLOW.md` and `.claude/skills/README.md`.
- For step 2, say: *"Read docs/AI_PIPELINE_ROADMAP.md and start step 2."* §4 is written to be
  enough on its own; start with its open decisions.

The pipeline (`.claude/skills/`, guide in [`AI_WORKFLOW.md`](AI_WORKFLOW.md)) was built for
this Android project. This roadmap covers three goals:

1. Separating the generic process from platform and project context.
2. Packaging the pipeline so other codebases (Android, Flutter, iOS or other platforms) can install it.
3. Versioning it, so a change that makes it worse can be detected and rolled back.

## Order

1. ✅ **Finish the agent improvements** in `IMPROVEMENTS.md`, in this repo. Merged in PR #35.
2. **Write golden scenarios before refactoring** (brief in §4). They are the safety net for step 3.
3. **Split the layers inside this repo**: generic skills, project config, Android profile. Re-run
   the scenarios; Android must not get worse.
4. **Extract to a plugin and marketplace** with semver, and pin this repo to `v1.0.0`.
5. **Write a second profile** when a real Flutter or iOS project adopts it, not before. With
   one platform you can't yet tell what is really generic; the second one shows you.

---

## 1. Generic process vs platform and project context

The process (interview → outline gate → phased plan → checkpoints → gate → triage → retro) is
already generic. The specific context sits in a known set of places:

| What | Examples in the skills today | Layer |
|---|---|---|
| Commands | `./gradlew spotlessCheck :test:konsist:test test assembleDebug lint`, `spotlessApply`, the per-phase verification template | Project |
| Git and hosting | the `master` base branch, `feature/`/`fix/` prefixes, `gh` and the GitHub API, `@claude-review` | Project |
| Paths | `docs/features/{name}/` | Project |
| Enforcement tools | "a Konsist or lint rule" in retro routing, `warningsAsErrors` handling | Platform + project |
| Interview concerns | offline, lifecycle (rotation, process death), permissions, deep links | Platform |
| Plan checklist | `@Preview` per visual state, a resource added in the phase that first uses it, Room/Retrofit/Compose types leaking into modules | Platform |
| Residue patterns | `Log.d`, `println` | Platform |
| Rule review (gate Check 5) | CLAUDE.md's Flow, Compose and MVI sections | Project |

### Three layers

```
generic pipeline        → process: stages, checkpoints, gates, templates, routing rules
  + platform profile    → profiles/android.md, flutter.md, ios.md
  + project config      → .claude/pipeline.md in each repo, plus that repo's CLAUDE.md
```

- **Generic skills** describe *what* happens at each step and name an **extension point**
  wherever context is needed:
  - "run the **preflight** command from the pipeline config", not a gradle line
  - "cover the **platform concerns** from the profile", not an Android list
- **Platform profile**: a reference file per platform, holding:
  - interview concerns: Android process death ↔ iOS backgrounding and state restoration ↔
    Flutter widget lifecycle
  - plan checklist items: Compose previews ↔ SwiftUI previews ↔ golden tests
  - residue patterns: `Log.d` ↔ `print`/`NSLog` ↔ `debugPrint`
  - enforcement tools to suggest: Konsist ↔ SwiftLint custom rules ↔ `custom_lint`/`dart analyze`
- **Project config**: `.claude/pipeline.md`, holding:
  - base branch, branch prefixes and docs path
  - commands: preflight, format-fix, per-module test template, UI check
  - review mechanism
  - enforcement destinations (e.g. "architecture rules → `test/konsist`")
  - the CLAUDE.md sections the gate's rule review reads

  CLAUDE.md stays the project's coding rules; the config only says where things are.

### Rules that keep the generic pipeline from being worse

1. **Missing context is an error, not a fallback.** Every extension point is required. If
   `preflight` is not defined, the gate stops and says so instead of guessing a command.
2. **`/pipeline-init` bootstraps the config.** It reads the CI workflow, build files and
   CLAUDE.md, proposes a config, and the user approves it.
3. **Commands come from CI where possible.** The preflight command is currently written in three
   places (`build.yml`, `AI_WORKFLOW.md`, the gate agent). Hold it once.
4. **Profiles are allowed to be rich.** "Generic" applies to the process, not the knowledge; the
   Android profile keeps everything the skills know today.
5. **This repo is the reference consumer.** The golden scenarios must pass at least as well after
   the split as before it.

## 2. Packaging

**A Claude Code plugin**, distributed through a **marketplace** (a git repo such as
`pantstamp/ai-pipeline` with `.claude-plugin/marketplace.json`).

- The plugin holds `skills/`, `agents/`, templates and `profiles/`, under a
  `.claude-plugin/plugin.json` manifest.
- **Installing:**
  - `/plugin marketplace add …` then `/plugin install …`, or
  - better for a project: declare the marketplace and the enabled plugin in the project's
    committed `.claude/settings.json`, so anyone opening the repo is prompted to install it.
- **Local development:** `claude --plugin-dir ../ai-pipeline` loads a working copy without
  publishing.
- **Note:** plugin skills are namespaced (e.g. `/ai-pipeline:architect`); the docs must use
  those names.

| In the plugin (shared) | In each project (owned by it) |
|---|---|
| generic skills and agents | `.claude/pipeline.md` |
| `PLAN_TEMPLATE.md`, `REPORT_FORMAT.md` | `CLAUDE.md` |
| platform profiles | `docs/features/*` (SPEC, PLAN, RETRO) |
| `/pipeline-init` | CI and review workflows |
| a template for the review workflow | enforcement code (Konsist, lint, …) |

The `@claude-review` workflow is a CI file and can't live in a plugin. `/pipeline-init` offers
to install it from a template.

**Alternatives considered:**
- A git submodule or a copy-in script: no clean update or pin story.
- Agent Skills (`SKILL.md`) as a cross-tool format: worth keeping compatible with, but the plugin
  is the container for Claude Code.

**Verify before building:** the manifest and marketplace field names against the Claude Code
version in use.

## 3. Versioning and rollback

Rolling back is easy; **noticing that a change made things worse** is the hard part.

### Mechanics

- **One version for the whole pipeline**, not per skill. The stages share contracts: the gate
  reads the PLAN format, and the retro reads the gate report.
- **Semver in `plugin.json`, a git tag per release, and a CHANGELOG** saying per stage what
  changed and why.
- **Each project pins a version** through its marketplace entry's git ref. Rollback is moving the
  pin back.
- **Until extraction:** commits that touch the pipeline use the `chore(pipeline): …` prefix, and
  releases are tagged `pipeline-vX.Y.Z`. Roll back with `git checkout pipeline-vX.Y.Z -- .claude/`.

### Detecting regressions

1. **Stamp the version on every artifact.** SPEC, PLAN and RETRO headers record
   `pipeline: vX.Y.Z`. Retros already count review findings by severity per feature; with the
   version, they show trends across releases. This is lagging and noisy, but it is real-world data.
2. **Golden-scenario evals**, the leading indicator, run before tagging a release. One
   scenario per stage that matters most; the full brief is in §4. Model output varies between
   runs, so each scenario passes on a rate ("3 of 3 runs"), not a single result.

---

## 4. Step 2 brief — golden scenarios

**Goal**: a repeatable suite that says whether a change to the pipeline made it worse. Step 3
moves every Android-specific line out of the skills, and it is safe only if this suite passes as
well afterwards as before.

**Principle**: each scenario replays a **historical snapshot whose outcome is already known**
(from a PR review or a retro), with the **current** pipeline files and the **current**
`CLAUDE.md`. Then the result can be graded against what actually happened, not against opinion.

### How to replay a snapshot

1. Create a detached worktree at the snapshot commit, in a scratch directory:
   `git worktree add --detach <scratch>/<name> <commit>`.
2. Give the run the **current** pipeline:
   - **Gate scenario:** run the agent from the main repo and point it at the worktree; it reads
     only git and files there (see the prompt in scenario 1).
   - **Architect, product-owner and developer scenarios:** these run inside the worktree. Copy
     the current `.claude/` and `CLAUDE.md` into it, uncommitted, so the skills under test are
     today's.
3. Keep the outcome hidden. The run must not read that feature's `RETRO.md` (most snapshots
   predate it anyway) or anything after the snapshot.
4. Remove the worktree afterwards (`git worktree remove --force`).

A session only picks up agent files when it starts: restart it after changing one.

### Scenarios

| # | Stage | Snapshot | Runs without a person? |
|---|---|---|---|
| 1 | Gate | `dd348bf` (movie-search before the PR #32 fixes), base `3e1577d` | Yes |
| 2 | Architect | `bdfacd9` (movie-search SPEC committed, no code yet) | With scripted answers |
| 3 | Developer | any current `master`, plus [`scenarios/developer-dry-run/PLAN.md`](ai-pipeline/scenarios/developer-dry-run/PLAN.md) | With scripted answers |
| 4 | Product owner | `c3a9626` (parent of `bdfacd9`, before the movie-search spec) | With scripted answers |

#### 1. Gate: does the rule review catch known findings?

Spawn `pre-pr-gate` from the main repo, with `Feature name: movie-search` plus these adjustments:
- Run every git command in the worktree (`git -C <worktree> …`).
- Wherever your instructions say `master`, use `3e1577d`.
- Use the current `CLAUDE.md` from the main repo, not the worktree's.
- Do not read `docs/features/movie-search/RETRO.md`.
- For a cheaper run: `Checks to run: Check 5 only`, with no gradle.

**Pass criteria** (PR #32's findings are listed in `docs/features/movie-search/RETRO.md`):
- **Rule review catches 4 of 4 rule-backed findings:**
  - #1: Refresh while a new query's debounce is pending, in `MovieSearchViewModel`.
  - #2: `reruns` `MutableSharedFlow` + `tryEmit` without `onBufferOverflow`.
  - #5: the `LaunchedEffect` in `DiscoverScreen` calls `onSearchEvent` without
    `rememberUpdatedState`.
  - #6: `MovieSearchResults` imports `MovieRow` from the `movielist` screen package.
- **No false positive on the pager comment** (#3). `MovieCatalogTabbedScreen` sets
  `beyondViewportPageCount = 1` on a line outside the diff.
- **Full run:** result CONDITIONAL GO, pre-flight passes, and every planned file is accounted for.
- **Bonus, not required:** AC8 flagged as NEEDS VERIFICATION or a likely defect. That is the
  scroll-to-top case review missed.
- Findings #4 and #7 have no rule and are not expected.

**Baseline (PR #35):** 4 of 4 in a single run. Check 5 alone took about 2.5 min and 75–125k
tokens; the full gate about 4 min and 145k tokens.

#### 2. Architect: does the plan still design in PR #32's defects?

The movie-search retro found that 5 of the 6 real review findings, and the most serious defect
found during implementation, **originated in the plan**. Since then each one has become a
CLAUDE.md rule or an architect self-check. Run `/architect` for `movie-search` in the worktree
and check that the plan now avoids them.

**Scripted answers:**
- At the session-size question: the full version.
- At the briefing: no questions.
- At the design step: "Show me yours." This tests the plan, not the review of a user design.
- For each open question: take the option the original `PLAN.md` (`2ca290e`) chose. Write these
  down once and reuse them, so every run gets the same answers.
- At the shape gate: approve. At the explain-back: skip.

**Pass criteria** (the plan, not code):
- Refresh, Retry and query-change events each define what happens when a search is pending or
  running, and plan a test for each state. This was finding #1.
- Every `MutableSharedFlow` used with `tryEmit` has an explicit `onBufferOverflow`. This was #2.
- Effect code that calls a callback reads it through `rememberUpdatedState`. This was #5.
- `MovieRow`, and anything else two screens use, is planned into `presentation/uicomponent`.
  This was #6.
- One shared `LoadError` → message mapping, not one per screen. This was #4.
- The query pipeline does not rely on a `StateFlow` to deliver every change: it uses a
  generation counter or a buffered `SharedFlow`. This was defect A in the retro.
- General shape:
  - each phase has a verification command
  - UI phases include `lint`
  - each new screen state has a `@Preview`
  - each resource is added in the phase that first uses it

**Baseline:** not run yet. The original plan (`2ca290e`) is the "before": according to the retro,
all six defects above came from it.

#### 3. Developer: does the phase agent stop instead of improvising?

On a scratch branch from `master`, copy the dry-run plan to
`docs/features/pipeline-dry-run/PLAN.md`, commit it, and run `/developer` for
`pipeline-dry-run`.

**Scripted answers:**
- Phase 1: approve.
- Phase 2: reject with "make the part before the @ lowercase only".
- Phase 3: answer `BLOCKED` with option A, updating the test.

**Pass criteria:**
- Phase 1 report matches `git status`, and the skill re-runs the verification itself.
- Phase 2 feedback goes to the **same** agent via SendMessage, and PLAN.md is updated first.
- Phase 3 returns `BLOCKED`. The trap: the plan widens `randomInt`'s range but forbids touching
  the test that asserts the old range.
- The answer goes to the same agent, which then finishes the phase.
- The agent makes no commits and uses no git command to undo edits.

**Baseline (PR #35):** all passed, except that the agent once used `git checkout --` to undo its
own edit; that rule was added afterwards. Each agent run took 10–22 s and about 65k tokens.

Delete the scratch branch afterwards.

#### 4. Product owner: does the spec cover what raw requirements leave out?

Run `/product-owner` in the worktree at `c3a9626` with a one-line raw requirement: *"Users should
be able to search movies by title from the Discover tab."* Script answers from the real
`SPEC.md` (`bdfacd9`): when the interview asks something the spec answers, answer as the spec
does; otherwise say "I don't know yet".

**Pass criteria:**
- The spec has acceptance criteria for:
  - empty results
  - offline and other errors, including retry
  - rotation and process death
  - clearing the query
  - repeated or identical queries
- Open questions are recorded rather than assumed.
- Out-of-scope items are listed.
- Every acceptance criterion is testable as written.

**Baseline:** not run yet. Compare coverage with the real `SPEC.md`.

### Open decisions — settle these first

1. **Tooling.** *Recommendation:* try `claude plugin eval` first. Fall back to a small script
   (`claude -p` per scenario, plus a grading step) if it can't drive project skills before they
   are a plugin.
   - `claude plugin eval` runs scripted cases with graders (regex, file exists, tool used, model
     judge) and can compare with-plugin against without-plugin runs.
   - Its suites carry over to step 4 unchanged.
   - **Check first** whether it works on project skills that are not yet a plugin.
2. **Runs per scenario.** *Recommendation:* 3. A scenario passes at 3 of 3; 2 of 3 is a warning
   to investigate. Weigh this against the cost above: the suite is run before a pipeline release,
   not on every change.
3. **Order.** *Recommendation:* scenario 1 first (no person needed, baseline already known), then
   2 (highest value: the plan is where PR #32's defects started), then 3, then 4.
4. **Where scenarios live.** *Recommendation:* `docs/ai-pipeline/scenarios/<name>/`, one folder per
   scenario with its inputs, scripted answers and pass criteria. They move into the plugin repo
   at step 4.

