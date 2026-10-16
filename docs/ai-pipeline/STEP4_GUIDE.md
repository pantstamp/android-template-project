# Step 4 guide: try the pipeline on the production project

Your checklist for roadmap step 4. Claude's own brief is [`HANDOFF.md`](HANDOFF.md); this file is
what **you** do, and what to check at each point.

**What this phase is for:** find every place the pipeline still assumes this template project,
before it is packaged as a plugin. It ends with a small patch release (`pipeline-v2.0.x`) here.

**The pipeline stays local to that laptop.** Nothing from it is committed or pushed in the
production repository: the files are copied in uncommitted, hidden from git by a local-only
exclude list (4.2), and deleted again at the end (part 6). Only the fixes are pushed, to this
repo.

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
- [ ] **Production does not track what the pipeline will add.** This must print nothing:

      ```bash
      git ls-files .claude/skills .claude/agents .claude/pipeline.md .claude/CHANGELOG.md .claude/PIPELINE_VERSION docs/features
      ```

      If it prints anything, production already has files there of its own. Tell Claude at the
      start, so the copy and the exclude list leave them alone.
- [ ] **Note what is there now**, to check against in part 6:

      ```bash
      git branch --list
      ```

      ```bash
      git log --branches --not --remotes --oneline
      ```

      The second lists local commits that are on no remote. Usually it prints nothing.
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
> project. The base branch is `…`, and the feature branch for the gate is `…`. Keep every
> pipeline file local to this laptop: nothing is committed or pushed in this repository.

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

- [ ] Right after the copy, `git status` shows only new (`??`) files under `.claude/` (`skills/`,
      `agents/`, `CHANGELOG.md`, `PIPELINE_VERSION`, `pipeline.md`), and **no modified file**. If
      production already had a `.claude/settings.json` or skills of its own, nothing of it changed.

Then Claude adds those paths, and `docs/features/` for the optional spec and plan, to
`.git/info/exclude`. That is git's ignore list that lives only in this clone; it is never committed
or pushed. From then on, `git add -A` and Android Studio's commit dialog skip the files, and the
skills still load, because Claude Code reads them from disk:

```bash
printf '%s\n' '.claude/skills/' '.claude/agents/' '.claude/pipeline.md' '.claude/CHANGELOG.md' '.claude/PIPELINE_VERSION' 'docs/features/' >> .git/info/exclude
```

(Leave out a line for any path production already tracks; see part 2.)

- [ ] `git status` is now **clean**, with the pipeline files still on disk.

### 4.3 The gate on the feature branch

Check out the feature branch you picked; the excluded `.claude/` files come along. Then:

> /pre-pr-checklist — feature name: `…`

**Expected:** the branch has no `SPEC.md` or `PLAN.md`, so Check 1 (plan coverage) and Check 2
(acceptance criteria) report that. That's fine. Look at the rest:

- [ ] It diffed against the right base branch
- [ ] The residue scan (Check 3) and git state (Check 4) make sense, and Check 4 does not report
      the pipeline files (they are excluded)
- [ ] The rule review (Check 5) quoted production's `CLAUDE.md`, not this template's
- [ ] The pre-flight ran production's CI tasks and read the result correctly
- [ ] Anything it did that only makes sense for the template (Konsist, `test/konsist`,
      `warningsAsErrors`, `@claude-review`) → a finding

### 4.4 Optional: the architect on a small real change

Only if a small change is coming anyway. Start with `/product-owner` (it creates a local feature
branch and the spec), then `/architect`. Stop after the plan; don't build it. This is the stage
most sensitive to wording, so it is worth one real run.

Both skills normally **commit** their SPEC.md and PLAN.md. Here they must not, so say it when you
start each one:

> /product-owner … — dry run: write SPEC.md, but do not commit it.

> /architect … — dry run: write PLAN.md, but do not commit it.

The files sit in `docs/features/`, which the exclude list hides. If a skill tries to commit
them anyway, git refuses: it will not add an excluded path without `-f`.

- [ ] Neither skill ran `git commit`, and `git status` is still clean

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
- [ ] Ask Claude to **remove exactly what the copy added**. The list is the tag's own files:

      ```bash
      git -C ~/src/android-template-project ls-tree -r --name-only pipeline-v2.0.0 .claude
      ```

      plus `.claude/pipeline.md` and the optional run's `docs/features/{feature-name}/` folder.
      Excluded files don't show in `git status`, so this list, not `git status`, says what to
      delete. If `.claude/settings.local.json` did not exist before the session, it can go too; it
      holds only this laptop's permission approvals.
- [ ] **Remove the lines added to `.git/info/exclude`.** Leave any lines that were there before.
- [ ] Delete any local branch the optional run created (`git branch -D …`). It was never pushed.
- [ ] **Check nothing is left.** `git status` is clean. `git branch --list` and `git log --branches
      --not --remotes --oneline` show what they showed in part 2.

## Done when

- [ ] Production's `pipeline.md` is written, reviewed and saved.
- [ ] The gate (and optionally the architect) ran on production, and every finding is listed.
- [ ] Every finding is fixed here or recorded as production's own gap.
- [ ] `pipeline-v2.0.x` is tagged, and the suite still passes.
- [ ] Production is as it was: its working tree is clean, its branches and local commits match
      part 2, and nothing was pushed to it.

Next is step 5, packaging the plugin, in this repo. See the roadmap's §2.
