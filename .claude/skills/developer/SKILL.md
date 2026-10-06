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

Each phase is implemented by the **`phase-implementer` agent** (`.claude/agents/phase-implementer.md`,
Sonnet), not in this conversation. Implementation is the bulky part of the work — file reads,
build output, fix loops — and running it in the agent keeps this conversation down to the plan,
the reports and the user's decisions for the whole feature. Every phase also starts from PLAN.md
alone, so a gap in the plan shows up immediately instead of being covered by what this session
remembers. This conversation keeps the checkpoint and the commit.

**a) Spawn the agent** in the foreground with the feature name and the phase number. Do not
paraphrase the phase for it: it reads PLAN.md itself, and a summary would replace the agreed
plan with your reading of it.

**b) Handle the result.**
- **`BLOCKED`** — put the question to the user. Then continue the **same** agent (SendMessage)
  with the answer, and handle its next result the same way. If the answer changes the plan,
  update PLAN.md first and tell the agent what changed.
- **`DONE`** — go to c).

**c) Check the report; don't take it on trust.**
- `git status --short` against the phase's file list in PLAN.md and the agent's own list. Any
  file the report doesn't mention, or the plan doesn't list, needs an explanation.
- Re-run the phase's verification command from the plan. Gradle's up-to-date checks keep this
  fast when nothing changed since the agent ran it. If it fails, show the user the error.

**d) Report to the user** concisely: files created and modified, any decision the plan left
open, anything done differently from the plan and why, and the verification result — the
agent's report, corrected by what c) found.

**e) Wait for approval.** Do not start the next phase until the user says to continue.

Between phases the user checks what no test covers: the app on a device, a preview in Android
Studio. When you check behaviour on a device or emulator yourself, confirm the precondition is
on screen (screenshot or log) before acting. An action taken while a spinner is still showing
tests the spinner.

If the user asks for changes, continue the **same** agent with their feedback, so it keeps its
context of the phase, then go back to c). If the feedback changes what the phase specifies,
update PLAN.md first and tell the agent to re-read it: PLAN.md is the agreed record, and the
pre-PR gate checks the code against it.

**f) Commit the approved phase.** Stage the phase's files by name — not `git add -A`, which also
sweeps in scratch files and IDE state. Ask about anything unexpected.

Record every manual check the user reported for this phase (on a device, emulator or preview)
as a `Verified-manually:` trailer, one per check, naming what was checked and where. The pre-PR
gate reads these from git to count a criterion as covered; it never sees this conversation.

```bash
git status --short
git add {files from this phase}
git commit -m "{type}({feature-name}): {phase title from the plan}" \
  -m "Verified-manually: {what was checked} ({device, emulator or preview})"
```

Per-phase commits give each checkpoint a rollback point, let a resumed session see progress in
`git log`, and give the pre-PR gate a real diff to check.

### 3. Resuming

PLAN.md is self-contained and every approved phase is a commit, so a new session resumes from
the branch: `git log --oneline master..HEAD` shows which phases are done. Uncommitted changes in
the working tree are a phase that was never approved — show them to the user before spawning
the next agent.

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
