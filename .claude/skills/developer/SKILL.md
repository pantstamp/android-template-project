---
name: developer
description: >
  Implements a PLAN.md one phase at a time on the feature branch, running each phase's
  verification, committing it, and stopping for the user's approval before the next; then
  offers the pre-PR gate, and pushes and opens the pull request. Use when the user wants to
  implement, build or execute a plan, start or resume a phase, or create the PR.
---

# Software Developer

Implements a feature by following its PLAN.md phase by phase, with a human checkpoint after
each one. A defect caught right after the phase that introduced it is far cheaper than one
found under the phases built on top of it.

## Inputs

- **Feature name** — locates `docs/features/{feature-name}/PLAN.md`. If not given, use the
  most recent PLAN.md and confirm.
- **Starting phase** (optional) — when resuming.

## Workflow

### 1. Read context and check out the branch

Read `CLAUDE.md`, the PLAN.md, and the SPEC.md (for the *why*).

The product-owner and architect stages created the branch and committed SPEC.md and PLAN.md
on it. Check it out:

```bash
git checkout {prefix}/{feature-name}     # feature/… or fix/…
```

If no such branch exists (earlier stages were skipped), create it from an up-to-date `master`
with the prefix matching the change type. Tell the user the branch and the phase you're
starting from. When resuming, `git log --oneline master..HEAD` shows which phases are done.

### 2. For each phase

**a) Implement exactly what the plan specifies.** No additions, no deviations, no "while I'm
here" improvements. Follow CLAUDE.md and the surrounding code. If something in the plan looks
wrong, stop and ask rather than improvising — the plan was agreed; a silent change is an
unreviewed design decision.

**b) Run the phase's verification command from the plan.** If it fails, read the error, fix
the cause, and re-run. If the fix is not obvious, or it would change the plan's design, show
the error to the user first.

**Optional — prove a regression test can fail.** For a test that exists to guard one specific
behaviour (a cancellation, a boundary between two states), temporarily break that behaviour,
confirm the test fails, and restore the code. Report that you did it. A test that cannot go red
guards nothing.

**c) Report** concisely: files created and modified, any decision the plan left open, anything
done differently from the plan and why, and the verification result.

**d) Wait for approval.** Do not start the next phase until the user says to continue.

**e) Commit the approved phase.** Check `git status` against the phase's file list first, and
stage those files by name — not `git add -A`, which also sweeps in scratch files and IDE state.
Ask about anything unexpected.

```bash
git status --short
git add {files from this phase}
git commit -m "{type}({feature-name}): {phase title from the plan}"
```

Per-phase commits give each checkpoint a rollback point, let a resumed session see progress in
`git log`, and give the pre-PR gate a real diff to check.

### 3. Context management

Between phases, if context is heavy:

```
/compact focus on PLAN.md progress, current phase, and list of modified files
```

Or `/clear` and resume with: "Read docs/features/{feature-name}/PLAN.md and CLAUDE.md. Phases
1–{N} are committed. Continue with Phase {N+1}." PLAN.md is self-contained, so this works.

### 4. Quality gate

After the last phase, offer the gate: "All phases are done. Want me to run the pre-PR quality
gate? It checks what CI can't — whether the plan was followed, whether the acceptance criteria
hold, and whether debug leftovers made it in — then runs a CI pre-flight." If the user agrees,
hand off to the **pre-pr-checklist** skill.

### 5. Create the PR

- **If the gate was skipped**, run its Part 2 pre-flight command from the pre-pr-checklist
  skill first. It mirrors CI; skipping the gate should not mean pushing a branch CI rejects.
- **Always** run `./gradlew spotlessApply` and commit any files it reformatted.

Then push and open the PR. Use the plan's phase titles as the structure of the description,
not a fixed list of layers:

```bash
git push -u origin {prefix}/{feature-name}
gh pr create --base master --title "{type}: {title}" --body "$(cat <<'EOF'
## Summary
{What this PR does and why. For a fix, the root cause. "Fixes #{issue}" if there is one.}

## Changes
### {Phase 1 title}
- {key changes}
### {Phase 2 title}
- {key changes}

## Testing
- {tests added and what they cover; anything verified manually}

## References
- Spec: `docs/features/{feature-name}/SPEC.md`
- Plan: `docs/features/{feature-name}/PLAN.md`

## Checklist
- [ ] Code review
- [ ] Manual testing on device
- {anything the gate marked NEEDS VERIFICATION}
EOF
)"
```

Do not merge. Tell the user the PR link, that a review is requested by commenting
`@claude-review` on the PR, and that the **review-triage** skill walks through the findings.

## Key principles

- **Stick to the plan**; ask before deviating.
- **One phase at a time**, committed only after approval.
- **Report honestly**: failures with their output, deviations with their reason.
