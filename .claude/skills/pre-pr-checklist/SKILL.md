---
name: pre-pr-checklist
description: >
  Run a pre-PR quality gate before creating a pull request. Checks the things
  CI cannot: whether the PLAN was actually followed, whether the SPEC's
  acceptance criteria are met, and whether debug leftovers or TODOs made it
  into the diff — then runs a CI pre-flight and reports go/no-go.
  Use this skill when the user is about to create a PR, has finished implementing
  a feature, wants to verify everything is ready before pushing, or mentions
  phrases like "pre-PR check", "ready to PR", "create PR", "push and create PR",
  "is this ready", "run checks", "quality gate", or "final check".
  Also trigger when the developer skill reaches the PR creation step.
---

# Pre-PR Quality Gate

## Why this exists alongside CI

CI and this skill answer different questions.

**CI checks that the code is correct.** It compiles, the tests pass, the
architecture rules hold, the formatting matches, lint is clean. All of that is
mechanical: a fixed set of rules applied to the code as it stands, with no
knowledge of what the change was supposed to do.

**This skill checks that it is the code you said you would write.** Whether
every file the PLAN listed actually exists, whether the SPEC's acceptance
criteria are addressed, whether a debug `println` or a `TODO` survived the last
commit. These require the SPEC and PLAN as a statement of intent and a judgment
about whether the diff honours it. A CI runner has neither the intent nor the
judgment — this is not a gap in the CI config, it is outside what CI can do.

That division is the answer when someone asks why the project has both:

| CI (`.github/workflows/build.yml`) | This skill |
|---|---|
| Does it compile? | Did you build what the plan specified? |
| Do the tests pass? | Do the acceptance criteria hold? |
| Do the architecture rules hold? | Is there debug residue in the diff? |
| Is it formatted, is lint clean? | Is the branch in a state worth reviewing? |

CI is authoritative for its half and re-runs everything on the PR regardless of
what happens here. So Part 1 below is the reason this skill exists; Part 2 is a
convenience that stops you pushing a branch CI will reject.

## Inputs

The user provides:
- **Feature name** — used to locate `docs/features/{feature-name}/PLAN.md` and `SPEC.md`

If not specified, infer from the current branch name (e.g., `feature/user-profiles`
→ `user-profiles`) and confirm with the user.

Establish the branch's diff once and reuse it for every check below:

```bash
git diff --name-only $(git merge-base HEAD master)..HEAD
```

This is the set of files the PR will contain. Prefer it over `HEAD~1..HEAD`,
which only sees the last commit and will miss most of a multi-commit branch.

---

## Part 1 — What CI cannot check

Run these first. They read files and the diff, so they cost seconds, and they
are the checks worth having a human in the loop for.

### Check 1: PLAN.md Coverage

Read `docs/features/{feature-name}/PLAN.md` and extract:
- Every file path listed under "Files to create"
- Every file path listed under "Files to modify"

Then verify against the branch diff:
- Every "Files to create" entry exists on disk
- Every "Files to modify" entry actually appears in the diff
- No files were changed that the plan never mentions (unexpected scope creep)

The third case is the one worth slowing down for. A file in the diff that is
absent from the plan is not necessarily wrong — plans miss things — but it is
the signal that the implementation drifted from what was agreed, and that is
exactly what the reviewer will ask about.

Report:
- Files in plan that are missing from disk
- Files in plan that show no changes
- Files changed that aren't in the plan (flag for review, not necessarily wrong)

### Check 2: SPEC.md Acceptance Criteria

Read `docs/features/{feature-name}/SPEC.md` and extract the acceptance criteria.
For each one, assess whether the implementation appears to address it, based on
the code in the diff.

This is a best-effort static reading, not a test run. Say so in the report. A
criterion that maps to a passing unit test is COVERED; one that depends on
runtime behaviour nobody exercised is NEEDS VERIFICATION even when the code
looks right. Flag what is unverified rather than assuming it works — an
overconfident PASS here is worse than an honest "needs a look", because it is
the last checkpoint before a reviewer's time is spent.

Report: List of criteria with COVERED / NEEDS VERIFICATION / NOT ADDRESSED.

### Check 3: Common Mistakes Scan

Scan the changed files for residue that compiles cleanly and passes every CI
check, which is precisely why CI will never catch it:

- **TODOs and FIXMEs**: any `TODO`, `FIXME`, `HACK`, `XXX` in added lines
- **Debug leftovers**: `println`, `Log.d`, `System.out`, hardcoded test values,
  a stubbed return that was meant to be temporary
- **Commented-out code**: blocks left behind from an earlier approach
- **Unused imports**: Spotless does not flag these — verified against this
  project's ktlint config, an unused import passes `spotlessCheck` — so if they
  matter to you, this scan is the only thing that will find them

Restrict the scan to added lines, not whole files. A pre-existing TODO in a file
you happened to touch is not yours to report.

Report: List of findings with file path and line number.

### Check 4: Git Status

```bash
git status
git diff --stat
```

Verify:
- No untracked files that should be committed (new files the PLAN listed)
- No uncommitted changes that look intentional
- Branch is up to date with `master` (no merge conflicts waiting)

An untracked file that the PLAN required is the failure mode worth catching
here: the build passes locally because the file is on disk, and fails in CI
because it was never added.

Report: Clean or list of concerns.

---

## Part 2 — CI pre-flight

CI is authoritative and re-runs all of this on the PR. This step exists only so
you do not spend a CI run and a review cycle discovering something a local
command would have told you.

```bash
./gradlew spotlessCheck :test:konsist:test test assembleDebug lint
```

Same tasks, same order as `.github/workflows/build.yml`. Gradle honours the
requested order for these, so the cheap checks still fail first, and the run
stops at the first failure. **If build.yml changes, change this line** — it is a
mirror, and a stale mirror is worse than none.

On failure, Gradle names the task that failed. Three of them want a specific
response rather than the fastest route to green:

- **`:test:konsist:test`** — a violated architecture rule. Fix the code to match
  the convention. If a rule genuinely needs to change, that is a conversation to
  have with the user, not a silent edit to the Konsist test.
- **`lint`** — the project sets `abortOnError = true` and `warningsAsErrors = true`,
  so one warning fails the build. Read the HTML report the task points to and fix
  the underlying issue. Do not add a baseline file or a suppression without
  asking first; a suppression is a decision about the project's standards.
- **`spotlessCheck`** — safe to auto-fix with `./gradlew spotlessApply`, then
  re-run. This is the one failure here you can resolve without a judgment call.

Report: PASS, or the failing task with its error.

---

## Output Format

Present results as a clear go/no-go report. Part 1 leads, because that is what
this gate is for:

```
══════════════════════════════════════════
  PRE-PR QUALITY GATE — {feature-name}
══════════════════════════════════════════

  What CI can't check
  ✅ Plan coverage   12/12 files accounted for
  ⚠️  Spec criteria   3/5 covered, 2 need manual verification
  ⚠️  Code scan       1 TODO in WatchedMoviesViewModel.kt:42
  ✅ Git status      Clean, no conflicts

  CI pre-flight
  ✅ spotless · konsist · test · assemble · lint   PASS

──────────────────────────────────────────
  RESULT: CONDITIONAL GO
──────────────────────────────────────────

  Action needed before PR:
  1. Resolve TODO at WatchedMoviesViewModel.kt:42
  2. Manually verify acceptance criteria #3 and #5

  Ready to create PR after addressing the above.
══════════════════════════════════════════
```

Use these result levels:
- **GO** — everything passes, ready to create PR
- **CONDITIONAL GO** — issues a reviewer should see but that do not block
- **NO GO** — the pre-flight failed, or the plan and the diff disagree in a way
  that needs resolving before anyone reviews it

A failed pre-flight is a NO GO because CI will fail the same way. Unaddressed
acceptance criteria are usually a CONDITIONAL GO — they need a human decision,
not a fix.

## After the report

If the result is GO:
- Ask the user if they want to proceed with PR creation (hand off to the
  developer skill's PR creation step)

If the result is CONDITIONAL GO:
- List the specific items to address
- Offer to fix what can be fixed automatically (TODOs, formatting)
- After fixes, re-run only the checks that failed

If the result is NO GO:
- Show the errors clearly
- Offer to help fix them, within the limits noted in Part 2
- After fixes, re-run the full gate from the top

## Integration with the developer skill

This skill slots in between Phase 4 (Testing) completion and PR creation:

1. User approves Phase 4 in the developer skill
2. User says "run pre-PR checks" or "is this ready for PR"
3. This skill runs the gate
4. On GO, the developer skill continues with PR creation

The developer skill's SKILL.md references this checkpoint. If the user skips it
and goes straight to "create the PR", that is fine — this is a recommended gate,
not a mandatory one.
