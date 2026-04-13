---
name: pre-pr-checklist
description: >
  Run a comprehensive pre-PR quality gate before creating a pull request.
  Verifies build, tests, lint, PLAN.md coverage, and common mistakes —
  then reports go/no-go with specific issues to fix.
  Use this skill when the user is about to create a PR, has finished implementing
  a feature, wants to verify everything is ready before pushing, or mentions
  phrases like "pre-PR check", "ready to PR", "create PR", "push and create PR",
  "is this ready", "run checks", "quality gate", or "final check".
  Also trigger when the developer skill reaches the PR creation step.
---

# Pre-PR Quality Gate

This skill runs a structured verification before creating a pull request.
It catches issues that are cheap to fix now but expensive to find in review —
a failed build, a missing test, a leftover TODO, a file the plan specified
but that was never created. Every issue caught here saves an API-billed
review cycle and a round of fix-push-re-review.

## Inputs

The user provides:
- **Feature name** — used to locate `docs/features/{feature-name}/PLAN.md` and `SPEC.md`

If not specified, infer from the current branch name (e.g., `feature/user-profiles`
→ `user-profiles`) and confirm with the user.

## Workflow

Run these checks in order. Stop and report if any critical check fails —
there's no point running lint if the build is broken.

### Check 1: Build Verification

```bash
./gradlew assembleDebug
```

This is the most basic gate. If it fails, nothing else matters.

Report: PASS or FAIL with the first compiler error.

### Check 2: Unit Tests

```bash
./gradlew testDebugUnitTest
```

Report: PASS (with count of tests run) or FAIL (with failing test names and
the assertion that failed).

### Check 3: Lint / Code Style

```bash
./gradlew ktlintCheck
```

If it fails, offer to auto-fix:
```bash
./gradlew ktlintFormat
```

Then re-run `ktlintCheck` to verify the fix worked. Report any remaining issues
that couldn't be auto-fixed.

### Check 4: PLAN.md Coverage

Read `docs/features/{feature-name}/PLAN.md` and extract:
- Every file path listed under "Files to create"
- Every file path listed under "Files to modify"

Then verify:
- Every "Files to create" entry exists on disk
- Every "Files to modify" entry has uncommitted or recent changes (check via `git diff`)
- No files were created that aren't mentioned in the plan (unexpected scope creep)

Report:
- Files in plan that are missing from disk
- Files in plan that show no changes
- Files changed that aren't in the plan (flag for review, not necessarily wrong)

### Check 5: SPEC.md Acceptance Criteria

Read `docs/features/{feature-name}/SPEC.md` and extract the acceptance criteria.
For each criterion, assess whether the implementation appears to address it based
on the code changes. This is a best-effort static check — flag anything that looks
unaddressed rather than making assumptions.

Report: List of criteria with COVERED / NEEDS VERIFICATION / NOT ADDRESSED.

### Check 6: Common Mistakes Scan

Scan the changed files (`git diff --name-only HEAD~1..HEAD` or the full branch diff)
for common issues:

- **TODOs and FIXMEs**: Any `TODO`, `FIXME`, `HACK`, `XXX` in changed lines
- **Debug leftovers**: `println`, `Log.d`, `System.out`, hardcoded strings that look
  like debug values
- **Commented-out code**: Blocks of commented code in changed files
- **Import cleanup**: Unused imports (if detectable from the diff)
- **Missing documentation**: Public classes or functions without KDoc (if the project
  convention requires it — check CLAUDE.md)

Report: List of findings with file path and line number.

### Check 7: Git Status

```bash
git status
git diff --stat
```

Verify:
- No untracked files that should be committed (new files mentioned in PLAN.md)
- No uncommitted changes that look intentional
- Branch is up to date with the base branch (no merge conflicts waiting)

Report: Clean or list of concerns.

## Output Format

Present results as a clear go/no-go report:

```
══════════════════════════════════════════
  PRE-PR QUALITY GATE — {feature-name}
══════════════════════════════════════════

  ✅ Build           PASS
  ✅ Tests           PASS (14 tests)
  ✅ Lint            PASS (auto-fixed 2 issues)
  ✅ Plan coverage   PASS (12/12 files accounted for)
  ⚠️  Spec criteria   3/5 covered, 2 need manual verification
  ⚠️  Code scan       1 TODO found in WatchedMoviesViewModel.kt:42
  ✅ Git status      Clean, no conflicts

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
- **GO** — all checks pass, ready to create PR
- **CONDITIONAL GO** — minor issues that should be reviewed but won't block
- **NO GO** — critical failures (build, tests) that must be fixed first

## After the report

If the result is GO:
- Ask the user if they want to proceed with PR creation (hand off to the
  developer skill's PR creation step)

If the result is CONDITIONAL GO:
- List the specific items to address
- Offer to fix what can be fixed automatically (TODOs, lint issues)
- After fixes, re-run only the failed checks to confirm

If the result is NO GO:
- Show the errors clearly
- Offer to help fix the build or test failures
- After fixes, re-run the full checklist from the top

## Integration with the developer skill

This skill is designed to slot in between Phase 4 (Testing) completion and
PR creation. The natural handoff is:

1. User approves Phase 4 in the developer skill
2. User says "run pre-PR checks" or "is this ready for PR"
3. This skill runs the quality gate
4. On GO, the developer skill continues with PR creation

The developer skill's SKILL.md references this checkpoint. If the user skips
it and goes straight to "create the PR", that's fine — this skill is a
recommended gate, not a mandatory one.
