# AI pipeline roadmap — from this project to a reusable package

Status: steps 1–3 done; step 3 is `pipeline-v2.0.0`. **Next: step 4**, the dry run on the
production project; the next session's brief is [`docs/ai-pipeline/HANDOFF.md`](ai-pipeline/HANDOFF.md). Re-planned on
2026-10-15 after a first attempt at a platform split regressed two stages (see §5).

**Starting a new session on this roadmap:** read this file, then `CLAUDE.md`,
`docs/AI_WORKFLOW.md`, `.claude/skills/README.md` and the HANDOFF.

The pipeline (`.claude/skills/`, guide in [`AI_WORKFLOW.md`](AI_WORKFLOW.md)) was built for this
Android project. The goals, in order:

1. **Use it on a second, production Android project**, installed and pinned to a version, with
   updates taken when that project chooses.
2. **Use it on a Flutter project**, once one adopts it.
3. **Detect a change that makes it worse, and roll it back** (§3), at every step.

This repo stays where the pipeline is developed and tested; the production project consumes
released versions.

## Order

1. ✅ **Finish the agent improvements** in `IMPROVEMENTS.md`. Merged in PR #35.
2. ✅ **Golden scenarios** (§4, suite in [`docs/ai-pipeline/`](ai-pipeline/README.md)). The
   safety net for every step below. All four pass 3 of 3 on `pipeline-v1.1.0`.
3. ✅ **Project config** (§1): move the facts that differ between Android projects into
   `.claude/pipeline.md`. The Android knowledge stays in the skills, word for word. Release as
   `pipeline-v2.0.0`.
4. **Dry run on the production project** (§1): write its `pipeline.md`, run the gate on an existing
   branch there, and fix in this repo every place a skill still assumes this template.
5. **Package as a plugin** (§2), in this repo, and switch this repo and the suite to it. Release as
   `pipeline-v3.0.0` (the command and agent names change).
6. **Install on the production project** (§2), pinned to the tag, and take one real feature
   through the whole pipeline, retro included.
7. **Flutter**, when a real Flutter project adopts it (§5). Not before: with one platform you
   cannot tell what is generic, and the first attempt to guess cost two regressions.

**Every release passes the suite**, and wherever a scenario drops below 3 of 3, it is compared with
the previous release **run the same day**: model behaviour drifts, so an old baseline is not proof.

---

## 1. Project config (steps 3 and 4)

Another Android project shares all of this one's platform knowledge: lifecycle and process death,
previews, lint, Compose and Flow pitfalls. What differs are facts, and only those move to
`.claude/pipeline.md`:

| Fact | This repo |
|---|---|
| Base branch, branch prefixes | `master`; `feature/`, `fix/`, `docs/` |
| Feature docs path | `docs/features/{feature-name}/` |
| Hosting | GitHub, `gh` |
| Pre-flight (mirrors CI), format fix, phase verification | `./gradlew spotlessCheck :test:konsist:test test assembleDebug lint`, `spotlessApply`, `:{module}:testDebugUnitTest assembleDebug` |
| Architecture-rule tool and where new rules go | Konsist, `test/konsist` |
| CLAUDE.md sections the gate's rule review reads | Flow and coroutines, Compose, MVI pattern |
| Review mechanism | `@claude-review`, `.github/workflows/claude-review.yml` |

**Rules:**
1. **Facts move; knowledge and wording do not.** A sentence keeps its 1.1.0 wording unless it
   names one of the facts above, and then only the fact is replaced.
2. **A missing fact is an error, not a fallback.** A stage that needs one the config lacks stops
   and says which.
3. **The pre-flight is held once**, in the config, mirroring CI.

**Step 4 tests the table.** Every edit the production project still needs in a skill is a fact
the table missed, or an assumption about this template (Konsist, CLAUDE.md's section names).
Fix those here, as a patch release, before packaging.

## 2. Packaging (steps 5 and 6)

Verified against Claude Code 2.1.296's plugin docs on 2026-10-15.

**Where it lives.** In this repo: the plugin in `plugins/ai-pipeline/` (skills, agents, templates,
CHANGELOG, `.claude-plugin/plugin.json` with the version), and `.claude-plugin/marketplace.json` at
the repo root listing it with source `./plugins/ai-pipeline`. The template app and the suite are
not shipped. A release is a `pipeline-vX.Y.Z` tag. The repo is public, so installing needs no
token; it can move to its own repo later by changing one line in each consumer.

**How a project installs it.** It commits `.claude/pipeline.md` and a `.claude/settings.json`:

```json
{
  "extraKnownMarketplaces": {
    "ai-pipeline": {
      "source": { "source": "github", "repo": "pantstamp/android-template-project", "ref": "pipeline-v3.0.0" },
      "autoUpdate": false
    }
  },
  "enabledPlugins": { "ai-pipeline@ai-pipeline": true }
}
```

Each developer runs `claude plugin install ai-pipeline@ai-pipeline --scope project` once, after
accepting the workspace trust dialog.

**How it updates.** Only when the project chooses: a PR moves `ref` to the new tag, then
`claude plugin update ai-pipeline@ai-pipeline`. Rollback moves it back.

**Developing it here.** `claude --plugin-dir ./plugins/ai-pipeline` loads the working copy over an
installed one; `/reload-plugins` picks up edits. `eval.py` loads it the same way.

**What changes with packaging:**
- Skills become `/ai-pipeline:architect` etc., agents `ai-pipeline:pre-pr-gate`; every reference
  by bare name is updated.
- Skills find their own files through `${CLAUDE_SKILL_DIR}` or `${CLAUDE_PLUGIN_ROOT}`, and the
  project's config through `${CLAUDE_PROJECT_DIR}/.claude/pipeline.md`. Files outside the plugin
  folder are not installed.
- Plugin agents ignore `permissionMode`, `hooks` and `mcpServers` in their frontmatter.
- The `@claude-review` workflow is a CI file and cannot ship in a plugin; the project copies it.

**Verify before relying on it:** the exact `subagent_type` string for a plugin agent, and that a
pinned tag is not refetched by auto-update.

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
  releases are tagged `pipeline-vX.Y.Z`. The version is in `.claude/PIPELINE_VERSION`, the
  CHANGELOG is `.claude/CHANGELOG.md`, and both move into the plugin at step 5. Roll back with
  `git checkout pipeline-vX.Y.Z -- .claude/`; the version file goes back with it.

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
   - Its suites carry over to step 5 unchanged.
   - **Check first** whether it works on project skills that are not yet a plugin.
2. **Runs per scenario.** *Recommendation:* 3. A scenario passes at 3 of 3; 2 of 3 is a warning
   to investigate. Weigh this against the cost above: the suite is run before a pipeline release,
   not on every change.
3. **Order.** *Recommendation:* scenario 1 first (no person needed, baseline already known), then
   2 (highest value: the plan is where PR #32's defects started), then 3, then 4.
4. **Where scenarios live.** *Recommendation:* `docs/ai-pipeline/scenarios/<name>/`, one folder per
   scenario with its inputs, scripted answers and pass criteria. They move into the plugin repo
   at step 5.


---

## 5. Platform split — deferred to the Flutter step

The first plan for step 3 split the skills into three layers: generic skills, an Android profile
(interview concerns, plan checklist, residue patterns) and a project config. It was built on
`chore/pipeline-split` (`1d62a57`…`84b5cab`) and measured on 2026-10-14:

| Stage | Split | `pipeline-v1.1.0`, same day |
|---|---|---|
| Gate | 3 of 3 | — |
| Developer | 3 of 3 (after a harness fix) | — |
| Product owner | 6 of 9 → 3 of 3 once the profile held the skill's lists verbatim | 3 of 3 |
| Architect | 3 of 6 → 1 of 3 with its generic wording restored | 3 of 3 |

**What it showed:**
- **Moving knowledge out of a skill, or rewording it on the way, made stages worse.** A profile
  that turned each concern into its own interview topic lengthened interviews by a third. An
  architect told to "meet the profile's plan checklist" instead of the checklist itself failed the
  `StateFlow` check (E6) in 4 of 9 runs.
- **Moving facts did not.** The product owner read its branch, prefixes and paths from the config
  and passed 3 of 3.
- **Concrete text beats lookups.** "Run `./gradlew … lint`" is followed more reliably than "run the
  config's UI check when the profile's condition holds".

**When Flutter comes**, keep one shared process and give each platform its own concrete text: either
skills generated from templates and a values file per platform, or two maintained editions. Choose
once a real Flutter project shows how much of the Android knowledge actually changes.
