# Step 4 guide: try the pipeline on the production project

Your checklist for roadmap step 4. Claude's own brief is [`HANDOFF.md`](HANDOFF.md); this file is
what **you** do, and what to check at each point.

**What this phase is for:** find every place the pipeline still assumes this template project,
before it is packaged as a plugin. Nothing is committed in the production project. It ends with a
small patch release (`pipeline-v2.0.x`) here.

**Time:** one session for parts 2–4, part of another for part 5. **Cost:** a few dollars on the
production side; $5–50 per golden-scenario re-run for the patch.

---

## Part 1 — Before you leave this laptop

- [ ] PR #44 (this guide and the HANDOFF section) is merged into `master`.

## Part 2 — Prepare the other laptop

- [ ] **Allowed?** Confirm that running Claude Code on the production code is allowed (company
      policy, client contract). If not, stop here.
- [ ] **Claude Code** is installed and logged in. Update it, then check the version (2.1.280 or newer)
      and that the pinned model answers:

      ```bash
      claude update
      ```

      ```bash
      claude --version
      ```

      ```bash
      claude -p "reply with ok" --model claude-opus-5-5
      ```
- [ ] **Android toolchain:** the production project builds from the command line (JDK 21 for
      Gradle, the Android SDK). Try its CI's main task once by hand, e.g. `./gradlew assembleDebug`.
- [ ] **GitHub CLI** is logged in with the account that owns this repo (it pushes the fixes):

      ```bash
      gh auth status
      ```
- [ ] **Clone this repo** next to the production project:

      ```bash
      git clone https://github.com/pantstamp/android-template-project.git ~/src/android-template-project
      ```
- [ ] **Production is clean:** in the production project, `git status` shows nothing, and you are
      on its base branch, pulled. Write down:
  - the base branch (`main` / `master` / `develop`)
  - its branch naming, if any
  - the CI workflow file
- [ ] **Pick a feature branch** for the gate: a recent, merged or open branch with a change of a few
      files to a few dozen. Write down its name.
- [ ] Optional: **pick a small upcoming change** for the architect, if one is coming anyway.

## Part 3 — Start the session

From the production project, with this repo added (adjust both paths):

```bash
cd ~/src/production-app && claude --add-dir ~/src/android-template-project
```

First message:

> Read `~/src/android-template-project/docs/ai-pipeline/HANDOFF.md` and run step 4 on this
> project. The base branch is `…`, and the feature branch for the gate is `…`.

## Part 4 — Your checkpoints during the session

Claude does the work; you check these.

### 4.1 The config it proposes

Claude reads production's CI, build files and `CLAUDE.md`, and proposes production's
`.claude/pipeline.md`. Check each value against what you know:

- [ ] `{{base}}` and the branch prefixes match how the team works
- [ ] `{{pre-flight}}` runs the **same tasks, in the same order, as CI**. Open the workflow file and
      compare.
- [ ] `{{format-fix}}` really fixes what the formatting check flags
- [ ] `{{verification}}` is a command that runs one module's unit tests. Try it once.
- [ ] `{{architecture-rules}}`: Konsist, detekt, something else, or **none**. If none, say so.
      Don't invent one.
- [ ] `{{rule-review}}` names sections that exist in production's `CLAUDE.md`. If production has
      no such rules, write that down; it is a finding, not something to paper over.
- [ ] `{{review}}` / `{{review-prompt}}`: does production have an automated review? If not, write
      that down too.

### 4.2 The copy

Claude copies the pipeline from the `pipeline-v2.0.0` tag into production's working tree,
uncommitted, so the files stay in place when you switch branches.

- [ ] `git status` shows only new files under `.claude/` (`skills/`, `agents/`, `CHANGELOG.md`,
      `PIPELINE_VERSION`, `pipeline.md`), and **no modified file**. If production already had a
      `.claude/settings.json` or skills of its own, nothing of it was changed.

### 4.3 The gate on the feature branch

Check out the feature branch you picked; the untracked `.claude/` files come along. Then:

> /pre-pr-checklist — feature name: `…`

**Expected:** the branch has no `SPEC.md` or `PLAN.md`, so Check 1 (plan coverage) and Check 2
(acceptance criteria) report that. That's fine. Look at the rest:

- [ ] It diffed against the right base branch
- [ ] The residue scan (Check 3) and git state (Check 4) make sense
- [ ] The rule review (Check 5) quoted production's `CLAUDE.md`, not this template's
- [ ] The pre-flight ran production's CI tasks and read the result correctly
- [ ] Anything it did that only makes sense for the template (Konsist, `test/konsist`,
      `warningsAsErrors`, `@claude-review`) → a finding

### 4.4 Optional: the architect on a small real change

Only if a small change is coming anyway. Start with `/product-owner` (it creates the feature branch
and the spec), then `/architect`. Stop after the plan; don't build it. This is the stage most
sensitive to wording, so it is worth one real run.

- [ ] The plan's verification commands are production's, not `./gradlew :test:konsist:test`
- [ ] The phases follow production's module structure

### 4.5 The findings list

Ask Claude for the list. Each finding is one of:

| Kind | Example | Where it is fixed |
|---|---|---|
| A fact the config missed | production runs detekt, which no name covers | a new name in `pipeline.md`'s table |
| A process gap | the gate cannot handle "no architecture tool" | a skill, in this repo |
| A gap in production | its `CLAUDE.md` has no coroutine rules | production's `CLAUDE.md`, by the team, later |

- [ ] **Read every finding for leaks before it goes anywhere.** This repo is public. Write
      findings as mechanisms ("a project without an architecture-rule tool"), never production's
      class names, paths, rules or code.

## Part 5 — Bring the fixes back here

In the same session (it can see this repo), or a new one in this repo:

- [ ] Claude makes the fixes on a branch in this repo. Read the diff: **no production names**.
- [ ] Claude runs the golden scenarios for the stages the fixes touch. Each must pass 3 of 3. If one
      misses a run, it compares with `pipeline-v2.0.0` the same day before changing anything
      (suite README, "Status").
- [ ] Release as `2.0.1` (or `2.1.0` if behaviour changed) following
      [`.claude/skills/README.md`](../../.claude/skills/README.md#versions). The CHANGELOG entry and
      `PIPELINE_VERSION` go in the PR.
- [ ] Merge the PR, then tag the merge commit `pipeline-v2.0.x`.

## Part 6 — Clean up production

- [ ] **Keep production's `pipeline.md`**: copy it somewhere safe outside the repo. It is needed again
      in step 6, when production installs the plugin and commits its config.
- [ ] Delete the copied pipeline files from the working tree (only the ones the copy added):

      ```bash
      git status --short .claude
      ```

      Remove each `??` path it lists. `git status` should then be clean.
- [ ] Delete any branch the optional architect run created, unless the team wants to keep its
      SPEC and PLAN.

## Done when

- [ ] Production's `pipeline.md` is written, reviewed and saved.
- [ ] The gate (and optionally the architect) ran on production, and every finding is listed.
- [ ] Every finding is fixed here or recorded as production's own gap.
- [ ] `pipeline-v2.0.x` is tagged, and the suite still passes.
- [ ] Production's working tree is clean.

Next is step 5, packaging the plugin, in this repo. See the roadmap's §2.
