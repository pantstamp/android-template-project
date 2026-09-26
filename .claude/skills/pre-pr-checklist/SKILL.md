---
name: pre-pr-checklist
description: >
  Runs a pre-PR quality gate on a feature branch: checks that the PLAN.md was followed, that
  each SPEC.md acceptance criterion is covered, and that no debug leftovers or TODOs are in
  the diff, then runs a local mirror of CI and reports GO, CONDITIONAL GO or NO GO. Use when
  the user asks whether a branch is ready for a PR, or for a pre-PR check, quality gate or
  final check.
---

# Pre-PR Quality Gate

CI checks that the code is correct. This gate checks that it is **the code that was agreed** —
which needs the SPEC and PLAN as intent and a judgment about the diff, neither of which CI has.
Part 1 is the reason the gate exists; Part 2 only saves a CI round-trip.

## Inputs

- **Feature name** — locates `docs/features/{feature-name}/PLAN.md` and `SPEC.md`. If not
  given, infer it from the branch name (`feature/user-profiles` → `user-profiles`) and confirm.

Establish the change set once and reuse it:

```bash
git diff --name-only $(git merge-base HEAD master)          # committed + uncommitted
git ls-files --others --exclude-standard                      # untracked
```

Diffing against the merge-base (not `HEAD~1`) covers every commit on the branch; including
the working tree covers the phase not yet committed.

## Part 1 — What CI cannot check

### Check 1: Plan coverage

From PLAN.md, collect every path under "Files to create" and "Files to modify". Verify:
- every file to create exists
- every file to modify appears in the change set
- nothing changed that the plan never mentions

Files under `docs/features/{feature-name}/` are expected. Any other unplanned file is not
necessarily wrong, but it is where the implementation drifted from what was agreed — flag it
with the reason if one is known.

### Check 2: Acceptance criteria

For each SPEC.md criterion, decide from the code and tests:
- **COVERED** — maps to a passing test, or to behaviour the user verified manually
- **NEEDS VERIFICATION** — depends on runtime behaviour nobody exercised, even if the code
  looks right
- **NOT ADDRESSED**

Say that this is a static reading. An overconfident COVERED is worse than an honest "needs a
look". For a UI criterion that is hard to reach on a device (an empty or rare error state),
suggest a `@Preview` of that state before any throwaway code change.

### Check 3: Residue scan (added lines only)

Things that compile and pass every CI check:
- `TODO`, `FIXME`, `HACK`, `XXX`
- debug output (`println`, `Log.d`, `System.out`), hardcoded test values, temporary stubs
- commented-out code
- unused imports — Spotless in this project does not flag them

A pre-existing TODO in a touched file is not this change's to report.

### Check 4: Git state

- Untracked files the plan requires — they build locally and fail in CI
- Uncommitted changes that look intentional
- Branch behind `master` or with conflicts (`git fetch` first)

## Part 2 — CI pre-flight

```bash
./gradlew spotlessCheck :test:konsist:test test assembleDebug lint
```

Same tasks and order as `.github/workflows/build.yml` — **if the workflow changes, change this
line.** Gradle keeps the requested order, so the cheap checks fail first and the run stops at
the first failure; keep that order. On failure:
- **`:test:konsist:test`** — fix the code to match the rule. Changing a rule is a conversation
  with the user, not a silent edit.
- **`lint`** (`warningsAsErrors`) — read the HTML report the task links to, and fix the cause.
  No baseline or suppression without asking.
- **`spotlessCheck`** — run `./gradlew spotlessApply` and re-run. The only failure here that
  needs no judgment call.

## Report

Use the format in [REPORT_FORMAT.md](REPORT_FORMAT.md). Result levels:
- **GO** — everything passes
- **CONDITIONAL GO** — things a reviewer should see that don't block; usually unverified
  criteria, which need a decision from the user rather than a fix
- **NO GO** — the pre-flight failed (CI will fail the same way), or plan and diff disagree in a
  way that needs resolving first

Then:
- **GO** → offer to continue with the developer skill's PR step
- **CONDITIONAL GO** → list the items; offer to fix what can be fixed; re-run only what failed
- **NO GO** → show the errors, offer fixes within the limits above, re-run the whole gate
